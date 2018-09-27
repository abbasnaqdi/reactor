### `Reactor`
[![](https://jitpack.io/v/dfmabbas/reactor.svg)](https://jitpack.io/#dfmAbbas/reactor)
[![License](http://img.shields.io/badge/license-MIT-green.svg?style=flat)](https://github.com/dfmabbas/reactor)
[![API](https://img.shields.io/badge/API-15%2B-blue.svg?style=flat)](https://github.com/dfmabbas/reactor)



##### `Reactor` is a `fast` and `secure` key-value library for Android, and has an embedded database based on the JSON structure and is a great alternative to Shared Performance.

##### `Note`: The password for the `AES` algorithm based on the `sign` key of the target application is made at `runtime` + `Hardware_ID` , so the encryption of this library is completely `safe`.

This library has a `very small size`.



#### Getting Started :

Add to your root build.gradle :

```Groovy
allprojects {
  repositories {
      maven { url 'https://jitpack.io' }
    }
  }
```

Add the dependency :
```Groovy
dependencies {
    implementation 'com.github.dfmabbas:reactor:v1.1.0'
}
```



#### Simple API (default) :

##### In `Kotlin`:
```Groovy
reactor = Reactor(context, Algorithm.AES)                                 

reactor.put("name", "abbas")
reactor.put("age", 23)
reactor.put("is_man", true)

reactor.put("any", this.context!!)


val name = reactor.get("name", "")
val age = reactor.get("age", 1)
val man = reactor.get("is_man", false)

val any = reactor.get("bb", this.context)

reactor.remove("age", 0)
reactor.clearAll()

```

##### In `Java` :
```Groovy
reactor = new Reactor(getContext(), Algorithm.AES);

reactor.put("name", "abbas");
reactor.put("age", 23);
reactor.put("is_man", true);

reactor.put("any", this.getContext());


String name = reactor.get("name", "");
Integer age = reactor.get("age", 1);
Boolean man = reactor.get("is_man", false);

Context any = reactor.get("bb", this.getContext());

reactor.remove("age", 0);
reactor.clearAll();
```



#### Advanced API :

##### In `Kotlin`:
######[Sample code written with Kotlin](sample/src/main/java/com/dfmabbas/sample/KotlinSample.kt) .

##### In `Java`:
######[Sample code written with Java](sample/src/main/java/com/dfmabbas/sample/JavaSample.java) .



#### License

    MIT License
    
    Copyright (c) 2018 Abbas Naghdi (@dfmabbas)
    
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
    SOFTWARE.

