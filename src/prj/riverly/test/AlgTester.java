package prj.riverly.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import prj.riverly.test.option.InputType;
import prj.riverly.test.option.PrintType;

public class AlgTester {

	private InputType inputType = InputType.PARAMETER;
	private PrintType printType = PrintType.PRINT_ALL;
	
	private Object target;
	private String methodName;
	private Object[] args;
	private Object result;
	
	private NumberFormat numberFormat = NumberFormat.getInstance();
	
	@SuppressWarnings("serial")
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
	
	@SuppressWarnings("serial")
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
	
	public AlgTester() {}
	
	public AlgTester(InputType inputType) {
		this.inputType = inputType;
	}
	
	public AlgTester input(InputType inputType) {
		this.inputType = inputType;
		return this;
	}
	
	public AlgTester clazz(String className, Object ...args) {
		try {
			Class<?> clazz = Class.forName(className);
			clazz(clazz, args);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return this;
	}
	
	public AlgTester clazz(Class<?> clazz) {
		this.target = newInstance(clazz);
		return this;
	}
	
	public AlgTester clazz(Class<?> clazz, Object ...args) {
		this.target = newInstance(clazz, args);
		return this;
	}
	
	public AlgTester object(Object object) {
		this.target = object;
		return this;
	}
	
	public AlgTester method(String method) {
		this.methodName = method;
		return this;
	}
	
	public AlgTester args(Object ...args) {
		this.args = args;
		return this;
	}
	
	public AlgTester result(Object result) {
		this.result = result;
		return this;
	}
	
	public AlgTester print(PrintType print) {
		this.printType = print;
		return this;
	}
	
	public Object test() {
		Method m = getMethod(target.getClass(), methodName);
		return test(target, m, args);
	}

	private Object test(Object obj, Method method, Object ...args) {
		if(method == null) {
			System.err.println("Invalid Method");
			return null;
		}
		
		Object[] invokeArgs = null;
		
		Class<?>[] types = method.getParameterTypes();
		int plen = types.length;
		
		if(inputType == InputType.SYSTEM_IN) {
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
			
		} else if(inputType == InputType.PARAMETER) {

			invokeArgs = Arrays.copyOf(args, args.length);
			
			if(!checkArguments(types, invokeArgs)) {
				System.err.println("Invalid Arguments");
				return null;
			}
			
		}
		
		try {
			StringBuffer sb = new StringBuffer();
			
			if(printType == PrintType.PRINT_ALL || printType == PrintType.PRINT_INPUT_ONLY) {
				sb.append(Arrays.deepToString(invokeArgs));
				sb.append(" => ");
			} else if(printType == PrintType.PRINT_MIN) {
				String param = Arrays.deepToString(invokeArgs);
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
			
			if(inputType == InputType.SYSTEM_IN) {
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
			
			if(printType == PrintType.PRINT_ALL || printType == PrintType.PRINT_RESULT_ONLY) {
				sb.append(toString(result));
				sb.append(" | ");
			} else if(printType == PrintType.PRINT_MIN) {
				String rstr = toString(result);
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
			
			if(this.result != null) {
				if(equlas(result, this.result)) sb.append("Success ");
				else sb.append("Fail ");
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
			int alen = args == null ? 0 : args.length;
			
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
		if(String.class.equals(type)) return str;
		
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
		
		String[] elements = splitByElement(str);
		len = elements.length;
		
		Object array = Array.newInstance(componentType, len);
		
		for(int i = 0; i < len; i++) {
			Object result = null;
			
			if(isArray) {
				result = parseArray(componentType, elements[i].trim());
			} else if(String.class.equals(componentType)) {
				result = String.valueOf(elements[i]);
			} else {
				result = parseDataType(componentType, elements[i].trim());
			}
			
			if(result == null) System.err.printf("Type Missmatch : %s to %s\r\n", elements[i], componentType.getName());
			
			Array.set(array, i, result);
		}
		
		return array;
	}
	
	private boolean isDataType(Class<?> type) {
		return dataTypeSet.contains(type);
	}
	
	private boolean equlas(Object obj, Object target) {
		if(obj == null && target == null) return true;
		if(obj == null || target == null) return false;
		
		if(target.getClass() == String.class) {
			return minimize(String.valueOf(target)).equals(minimize(toString(obj)));
		}
		
		Class<?> clazz = obj.getClass();
		
		if(clazz != target.getClass()) return false;
		
		if(isDataType(clazz) || clazz.isEnum()) {
			return target.equals(obj);
		} else if(clazz.isArray()) {
			int len = Array.getLength(obj);
			int len2 = Array.getLength(target);
			
			if(len != len2) return false;
			
			for(int i = 0; i < len; i++) {
				if(!equlas(Array.get(obj, i), Array.get(target, i))) return false;
			}
			
			return true;
		} else if(obj instanceof Collection || obj instanceof Map) {
			return obj.equals(target);
		} else {
			Field[] fields = clazz.getDeclaredFields();
			int len = fields.length;
			
			for(int i = 0; i < len; i++) {
				Field field = fields[i];
				try {
					field.setAccessible(true);
				} catch(InaccessibleObjectException e) {
					continue;
				}
				
				try {
					if(!equlas(field.get(obj), field.get(target))) return false;
				} catch (IllegalArgumentException | IllegalAccessException e) {
				}
			}
			
			return true;
		}
	}
	
	private String toString(Object obj) {
		if(obj == null) return "null";
		Class<?> clazz = obj.getClass();
		StringBuilder sb = new StringBuilder();
		
		if(isDataType(clazz) || clazz.isEnum()) {
			return String.valueOf(obj);
		} else if(clazz == String.class) {
			sb.append(obj);
			return sb.toString();
		} else if(clazz.isArray() || obj instanceof Collection) {
			sb.append("[");
		
			int len = foreach(obj, v -> {
				sb.append(toString(v));
				sb.append(",");
			});
			
			if(len > 0) sb.setLength(sb.length()-1);
			
			sb.append("]");
			return sb.toString();
		} else if(obj instanceof Map) {
			Map<?,?> map = (Map<?,?>) obj;
			Set<?> keySet = map.keySet();
			sb.append("{");

			int len = foreach(keySet, key -> {
				sb.append(toString(key));
				sb.append(":");
				sb.append(toString(map.get(key)));
				sb.append(",");
			});
			
			if(len > 0) sb.setLength(sb.length()-1);

			sb.append("}");
			return sb.toString();
		} else {
			sb.append("{");
			
			Field[] fields = clazz.getDeclaredFields();
			int len = fields.length;
			
			for(int i = 0; i < len; i++) {
				Field field = fields[i];
				try {
					field.setAccessible(true);
				} catch(InaccessibleObjectException e) {
					continue;
				}
				
				sb.append(field.getName());
				sb.append(":");
				
				try {
					sb.append(toString(field.get(obj)));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					sb.append("null");
				}
				sb.append(",");
			}
			if(len > 0) sb.setLength(sb.length()-1);

			sb.append("}");
			return sb.toString();
		}
		
	}
	
	private String[] splitByElement(String array) {
		int len = array.length();
		
		int stack = 0;
		int off = 1;
		
		ArrayList<String> list = new ArrayList<String>();
		
		for(int i = 1; i < len-1 ; i++) {
			char ch = array.charAt(i);
			
			if(ch == '[' || ch == '{') stack++;
			else if(ch == ']' || ch == '}') stack--;
			else if(stack == 0 && ch == ',') {
				list.add(array.substring(off, i).trim());
				off = i+1;
			}
		}
		
		list.add(array.substring(off,len-1).trim());
		
		int resultSize = list.size();
		
		while(resultSize > 0 && list.get(resultSize-1).length() == 0) resultSize--;
		
		String[] result = new String[resultSize];
		return list.subList(0, resultSize).toArray(result);
	}
	
	private int foreach(Object obj, Consumer<? super Object> consumer) {
		Class<?> clazz = obj.getClass();
		
		if(clazz.isArray()) {
			int len = Array.getLength(obj);
			for(int i = 0; i < len; i++) {
				consumer.accept(Array.get(obj, i));
			}
			return len;
		} else if(obj instanceof Collection) {
			Collection<?> collection = (Collection<?>) obj;
			Iterator<?> iter = collection.iterator();
			int count = 0;
			
			while(iter.hasNext()) {
				consumer.accept(iter.next());
				count++;
			}
			return count;
		}
		
		return 0;
	}
	
	private String minimize(String str) {
		return str.replaceAll(" ", ""); 
	}
	
}
