## 알고리즘 테스터
자바 소스로 작성된 알고리즘을 테스트 하는 프로그램입니다.  
백준 알고리즘, 프로그래머스 등 알고리즘 문제를 풀때 테스트 해 볼 수 있습니다.  

## 사용법
```java
AlgTester algTester = new AlgTester(InputType.PARAMETER);
algTester.clazz(Main.class).method("main");
algTester.args(1).result(2).test();
```


AlgTester 객체를 생성합니다.  
생성자에 InputType을 설정할 수 있습니다.

InputType  
SYSTEM_IN : 표준 입력으로 값이 입력됩니다.  
PARAMETER : 메소드의 파라미터에 값을 넘겨주게 됩니다.   

```java
AlgTester algTester = new AlgTester(InputType.PARAMETER);
```
  
  
테스트 할 알고리즘을 작성한 클래스와 메소드를 지정합니다.

```java
algTester.clazz(Main.class).method("main");
```

테스트 값을 입력 합니다.  


```java
algTester.args(1);

// 입력값이 배열일 경우 다음과 같이 문자열로 입력해도 됩니다.
// algTester.args("[1, 2]");
```

결과 값을 설정하면 테스트한 결과가 맞는지 틀린지 확인 할 수 있습니다.  

```java
algTester.result(2);
```

테스트를 수행합니다.  
메소드를 수행한 결과값을 반환합니다.  
InputType이 <i><span style="color:#0000FF">SYSTEM_IN</span></i>인 경우 결과값을 System.out에서 받아옵니다.  
InputType이 <i><span style="color:#0000FF">PARAMETER</span></i>인 경우 결과값을 메소드의 반환값에서 받아옵니다.

```java
Object result = algTester.test();
```


다음과 같이 테스트 결과가 출력됩니다.
```
[1] => 2 | Success 1.252ms, 0Kbyte
```
[입력값] => 결과값 | 성공or실패 소요시간 사용메모리


## 옵션
#### InputType  
- SYSTEM_IN : 표준 입력으로 값이 입력됩니다.  
- PARAMETER : 메소드의 파라미터에 값을 넘겨주게 됩니다.  

#### PrintType  
- PRINT_ALL : 입력값과 결과값 모두 출력합니다.  
- PRINT_INPUT_ONLY : 입력값만 출력합니다.  
- PRINT_RESULT_ONLY : 결과값만 출력합니다.  
- PRINT_MIN : 입력값과 결과값이 20자를 초과하는 경우 잘라서 출력합니다.  
- PRINT_NONE : 결과를 출력하지 않습니다.

