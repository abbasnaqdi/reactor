##### `Reactor`

[![](https://jitpack.io/v/aaaamirabbas/reactor.svg)](https://jitpack.io/#dfmAbbas/reactor)
[![License](http://img.shields.io/badge/license-MIT-green.svg?style=flat)](https://github.com/aaaamirabbas/reactor)
[![API](https://img.shields.io/badge/API-15%2B-blue.svg?style=flat)](https://github.com/aaaamirabbas/reactor)

**Reactor** is a `fast` and `secure` key-value library for Android, and has an embedded database based on the JSON structure and is a great alternative to Shared Preferences.

##### Features + Road map

###### First Edition 1.x.x

- [x] `Save and restore a variety of objects (serialization and deserialization)`
- [x] `Symmetric encryption of objects (signed by target application at runtime + Hardware_ID)`
- [x] `Very high performance‍`
- [x] `Very low library size (No need for other libraries)`
- [x] `Supported and tested in API 15 and above`
- [x] `Minimal and easy to use :)`

###### Second Edition 2.x.x

- [ ] `Save and restore all temporary object pool at runtime in RAM`
- [ ] `Add a data branch (branches can be independent of the main branch) `
- [ ] `Imports safe data from Shared Preferences to Reactor`
- [ ] ‍‍`Change the underlying AES password generation`
- [ ] `Change the storage infrastructure`
- [ ] ‍‍‍`Add concurrency + thread-safe functionality`

[![Donate](https://img.shields.io/badge/Cryptocurrency-Donate-green)](https://idpay.ir/aaaamirabbas) **BTC**: `1HPZyUP9EJZi2S87QrvCDrE47qRV4i5Fze`

[![Donate](https://img.shields.io/badge/Cryptocurrency-Donate-blue)](https://idpay.ir/aaaamirabbas) **ETH or USDT**: `0x4a4b0A26Eb31e9152653E4C08bCF10f04a0A02a9`

##### Getting Started :

Add to your root build.gradle :Ï

```java
allprojects {
  repositories {
      ...
      maven { url 'https://jitpack.io' }
    }
  }
```

Add the dependency :

```java
dependencies {
    implementation 'com.github.aaaamirabbas:reactor:1.5.2'
}
```

##### Simple API (default) :

In `Kotlin` :

```java
val reactor = Reactor(context)
val reactor = Reactor(context, false) // disable encryption

-----------------------------------------------------------

reactor.put("firstName", "abbas")
reactor.put("lastName", null)
reactor.put("age", 23)
reactor.put("customDataClass", SampleData())

-----------------------------------------------------------

val firstName = reactor.get<String>("firstName")
val lastName : String? = reactor.get("lastName")
val isDay = reactor.get<Boolean>("isDay", false)
val customDataClass = reactor.get("customDataClass")

-----------------------------------------------------------

reactor.remove<Int>("year", "week")
reactor.eraseAllData()
```

In `Java` :

```java
Reactor reactor = new Reactor(getContext());
Reactor reactor = new Reactor(getContext(), false); // disable encryption

-----------------------------------------------------------

reactor.put("firstName", "abbas");
reactor.put("lastName", null);
reactor.put("age", 23);
reactor.put("customDataClass", new SampleData());

-----------------------------------------------------------

String firstName = reactor.get("firstName", "abbas");
String lastName = reactor.get("lastName", null);
Integer age = reactor.get("age", 26);
SampleData customDataClass = reactor.get("array");

-----------------------------------------------------------

reactor.remove("age", 0);
reactor.eraseAllData();
```

##### Custom data class Sample :

```Kotlin
// definition
data class SampleData(
    val id: Int = 24,
    val name: String = "abbas"
) : ReactorContract

-----------------------------------------------------------

// save, restore, remove
reactor.put("simpleData", SampleData())
reactor.get<SampleData>("simpleData") // return null if is not found
reactor.remove<SampleData>("simpleData")
```

##### FAQ :

###### Need more help?

- [Check out the classes in this folder](sample/src/main/java/com/aaaamirabbas/sample)

###### How to store and restore the custom class ?

- [See this issue : #1](https://github.com/aaaamirabbas/reactor/issues/1)
