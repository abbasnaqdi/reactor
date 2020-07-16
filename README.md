##### `Reactor`

[![](https://jitpack.io/v/oky2abbas/reactor.svg)](https://jitpack.io/#dfmAbbas/reactor)
[![License](http://img.shields.io/badge/license-MIT-green.svg?style=flat)](https://github.com/oky2abbas/reactor)
[![API](https://img.shields.io/badge/API-15%2B-blue.svg?style=flat)](https://github.com/oky2abbas/reactor)

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

- [ ] `Save and restore all objects at runtime in RAM`
- [ ] `Add a data branch (branches can be independent of the main branch) `
- [ ] `Imports data from Shared Preferences to Reactor`
- [ ] ‍‍`Change the underlying AES password generation`
- [ ] `Change the storage infrastructure`
- [ ] ‍‍‍`Add concurrency + thread-safe functionality`



[![Donate](https://img.shields.io/badge/Donate-green)](https://idpay.ir/oky2abbas)

**Bitcoin (BTC) Donate: `bc1qhgvnx2nfzr0qep5fnsevyyn59k32wpe7q0c7nh`**

**Ethereum (ETH) Donate: `0x0dA44bbcc2d7BBF11eb070A81CB24c4eE7Bf1AD9`**




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
    implementation 'com.github.oky2abbas:reactor:v1.2.5'
}
```

##### Simple API (default) :

In `Kotlin` :

```java
val reactor = Reactor(context)
val reactor = Reactor(context, false) // disable encryption

-----------------------------------------------------------

reactor.put("name", "abbas")
reactor.put("age", 23)
reactor.put("this", this::class.java)

-----------------------------------------------------------

val name = reactor.get("name", "")
val isDay = reactor.get("day", false)
val thisClass = reactor.get("this", this::class.java)

-----------------------------------------------------------

reactor.remove("day", false)
reactor.clearAll()
```

In `Java` :

```java
Reactor reactor = new Reactor(getContext());
Reactor reactor = new Reactor(getContext(), false); // disable encryption

-----------------------------------------------------------

reactor.put("name", "abbas");
reactor.put("age", 23);
reactor.put("array", new int[]{0, 0, 0});

-----------------------------------------------------------

String name = reactor.get("name", "");
Integer age = reactor.get("age", 0);
int[] array = reactor.get("array", new int[]{0, 0, 0});

-----------------------------------------------------------

reactor.remove("age", 0);
reactor.clearAll();
```

##### FAQ :

###### Need more help?

- [Check out the classes in this folder](sample/src/main/java/com/oky2abbas/sample)

###### How to store and restore the custom class ?

- [See this issue : #1](https://github.com/oky2abbas/reactor/issues/1)

##### License

```
MIT License

Copyright (c) 2020  abbas naqdi

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE
```
