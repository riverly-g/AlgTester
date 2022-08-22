package prj.rivery.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Tester {

	public static short DEBUG_ALWAYS = 0;
	public static short DEBUG_FAILURE = 1;
	
	public static short SYSTEM_IN = 0;
	public static short PARAMETER = 1;
	
	public static short PRINT_ALL = 0;
	public static short PRINT_PARAMETER_ONLY = 1;
	public static short PRINT_RESULT_ONLY = 2;
	public static short PRINT_MIN = 3;
	public static short PRINT_NONE = 4;
	
	private short inputType = 0;
	private short printType = 0;
	
	private Object target;
	private String methodName;
	private Object[] args;
	private Object[] results;
	
	private NumberFormat numberFormat = NumberFormat.getInstance();
	
	private HashSet<Class<?>> dataTypeSet = new HashSet<Class<?>>(){{
		add(byte.class);
		add(Byte.class);
		add(char.class);
		add(Character.class);
		add(boolean.class);
		add(Boolean.class);
		add(short.class);
		add(Short.class);
		add(int.class);
		add(Integer.class);
		add(long.class);
		add(Long.class);
		add(float.class);
		add(Float.class);
		add(double.class);
		add(Double.class);
	}};
	
	private HashMap<Class<?>, Class<?>> primitiveTypeMap = new HashMap<Class<?>, Class<?>>(){{
		put(byte.class, Byte.class);
		put(char.class, Character.class);
		put(boolean.class, Boolean.class);
		put(short.class, Short.class);
		put(int.class, Integer.class);
		put(long.class, Long.class);
		put(float.class, Float.class);
		put(double.class, Double.class);
	}};
	
	public Tester() {}
	
	public Tester(short inputType) {
		this.inputType = inputType;
	}
	
	public Tester input(short inputType) {
		this.inputType = inputType;
		return this;
	}
	
	public Tester clazz(String className, Object ...args) {
		try {
			Class<?> clazz = Class.forName(className);
			clazz(clazz, args);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return this;
	}
	
	public Tester clazz(Class<?> clazz, Object ...args) {
		this.target = newInstance(clazz, args);
		return this;
	}
	
	public Tester object(Object object) {
		this.target = object;
		return this;
	}
	
	public Tester method(String method) {
		this.methodName = method;
		return this;
	}
	
	public Tester args(Object ...args) {
		this.args = args;
		return this;
	}
	
	public Tester results(Object ...results) {
		this.results = results;
		return this;
	}
	
	public Tester print(short print) {
		this.printType = print;
		return this;
	}
	
	public Object test() {
		Method m = getMethod(target.getClass(), methodName);
		return test(target, m, args);
	}

	private Object test(Object obj, Method method, Object ...args) {
		if(method == null) {
			System.out.println("Invalid Method");
			return null;
		}
		
		Object[] invokeArgs = null;
		
		Class<?>[] types = method.getParameterTypes();
		int plen = types.length;
		
		if(inputType == SYSTEM_IN) {
			StringBuilder sb = new StringBuilder();
			int len = args.length;
			
			for(int i = 0; i < len; i++) {
				sb.append(String.valueOf(args[i]));
				sb.append("\r\n");
			}
						
			InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
			System.setIn(is);
			
			invokeArgs = new Object[plen];
			
			for(int i = 0; i < plen; i++) invokeArgs[i] = null;
			
		} else if(inputType == PARAMETER) {

			invokeArgs = args;
			
			if(!checkArguments(types, invokeArgs)) {
				System.out.println("Invalid Arguments");
				return null;
			}
		}
		
		try {
			StringBuffer sb = new StringBuffer();
			
			if(printType == PRINT_ALL || printType == PRINT_PARAMETER_ONLY) {
				sb.append(Arrays.deepToString(args));
				sb.append(" => ");
			} else if(printType == PRINT_MIN) {
				String param = Arrays.deepToString(args);
				int len = param.length();
				
				if(len > 20) {
					param = param.substring(0, 20);
					param = param.replaceAll("(\r\n|\r|\n|\n\r)", " ");
					sb.append(param);
					sb.append("...");
					sb.append(len);
				} else {
					sb.append(param);
				}
				
				sb.append(" => ");
			}

			Object result = null;
			Performance performance = new Performance();
			
			if(inputType == SYSTEM_IN) {
				PrintStream p = System.out;
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				System.setOut(new PrintStream(bos));
				
				performance.start();
				method.invoke(obj, invokeArgs);
				performance.end();
				
				System.setOut(p);
				
				String str = bos.toString();
				
				int len = str.length()-1;
				
				if(len >= 0) {
					char t;
					while((t = str.charAt(len)) == '\n' || t == '\r') len--;
					str = str.substring(0, len+1);
				}
				
				result = str;
			} else {
				performance.start();
				result = method.invoke(obj, invokeArgs);
				performance.end();
			}
			
			if(printType == PRINT_ALL || printType == PRINT_RESULT_ONLY) {
				sb.append(result);
				sb.append(" | ");
			} else if(printType == PRINT_MIN) {
				String rstr = String.valueOf(result);
				int len = rstr.length();
				
				if(len > 20) {
					rstr = rstr.substring(0, 20);
					rstr = rstr.replaceAll("(\r\n|\r|\n|\n\r)", " ");
					sb.append(rstr);
					sb.append("...");
					sb.append(len);
				} else {
					sb.append(rstr);
				}
				
				sb.append(" | ");
			}
			
			sb.append(numberFormat.format(performance.getTime()/1000000.0));
			sb.append("ms, ");
			sb.append(numberFormat.format(performance.getMemory()/1000.0));
			sb.append("Kbyte");
			
			System.out.println(sb.toString());
			sb.setLength(0);
			
			return result;
			
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Object newInstance(Class<?> clazz, Object ...args) {
		Constructor<?> result = getConstructor(clazz, args);
		result.setAccessible(true);
		
		try {
			if(result.getParameterCount() > 0) return result.newInstance(args);
			else return result.newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private Constructor<?> getConstructor(Class<?> clazz, Object ...args) {
		Constructor<?> result = null;
		
		try {
			Constructor<?>[] constructors = clazz.getDeclaredConstructors();

			boolean match = false;
			int len = constructors.length;
			int alen = args.length;
			
			Constructor<?> noArgs = null;
			
			for(int i = 0; i < len; i++) {
				Constructor<?> constructor = constructors[i];
				
				if(constructor.getParameterCount() == alen && checkArguments(constructor.getParameterTypes(), args)) {
					result = constructor;
					match = true;
					break;
				}
				
				if(constructor.getParameterCount() == 0) noArgs = constructor;
			}
			
			if(!match) {
				if(noArgs != null) result = noArgs;
			}
			
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private Method getMethod(Class<?> clazz, String method) {
		Method[] methods = clazz.getDeclaredMethods();
		int len = methods.length;
		
		for(int i = 0; i < len; i++) {
			Method m = methods[i];
			if(m.getName().equals(method)) {
				m.setAccessible(true);
				return m;
			}
		}
		
		return null;
	}
	
	private boolean checkArguments(Class<?>[] types, Object[] args) {
		
		int len = types.length;
		int alen = args.length;
		
		if(len != alen) return false;
		
		for(int i = 0; i < len; i++) {
			Class<?> type = types[i];
			Object arg = args[i];
			
			if(!checkType(type, arg)) {
				if(String.class.equals(arg.getClass())) {
					Object result = parse(type, String.valueOf(arg));
					
					if(result != null) { 
						args[i] = result; 
					} else { 
						return false; 
					}
					
				} else {
					return false;
				}
			}
		}
		
		return true;
	}
	
	private boolean checkType(Class<?> type, Object obj) {
		Class<?> a = type;
		Class<?> b = obj.getClass();
		
		if(a.isPrimitive() != b.isPrimitive()) {
			if(a.isPrimitive()) a = primitiveTypeMap.get(a);
			if(b.isPrimitive()) b = primitiveTypeMap.get(b);
		}
		
		return a.equals(b);
	}
	
	private Object parse(Class<?> type, String str) {
		if(String.class.equals(type)) return true;
		
		if(type.isArray()) {
			return parseArray(type, str);
		}

		return parseDataType(type, str);
	}
	
	private Object parseDataType(Class<?> type, String str) {
		if(!dataTypeSet.contains(type)) return null;
		
		Class<?> wrapperType = type.isPrimitive() ? primitiveTypeMap.get(type) : type ;
		
		Method[] methods = wrapperType.getMethods();

		for(int i = 0; i < methods.length; i++) {
			if(methods[i].getName().startsWith("parse") && methods[i].getParameterCount() == 1) {
				try {
					return methods[i].invoke(wrapperType, str);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					return null;
				}
			}
		}
		
		return null;
	}
	
	private Object parseArray(Class<?> type, String str) {
		if(!type.isArray()) return null;
		
		int len = str.length();
		
		if(str.charAt(0) != '[' || str.charAt(len-1) != ']') return null;
		
		Class<?> componentType = type.getComponentType();
		boolean isArray = componentType.isArray();
		
		String[] elements = str.substring(1, len-1).split(",");
		len = elements.length;
		
		Object array = Array.newInstance(componentType, len);
		
		for(int i = 0; i < len; i++) {
			Object result = null;
			
			if(isArray) {
				result = parseArray(componentType, elements[i].trim());
			} else {
				result = parseDataType(componentType, elements[i].trim());
			}
			
			if(result == null) return false;
			
			Array.set(array, i, result);
		}
		
		return array;
	}
	
	private boolean isDataType(Class<?> type) {
		return dataTypeSet.contains(type);
	}
	
}
