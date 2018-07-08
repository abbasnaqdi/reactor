## ReactorDB
[![](https://jitpack.io/v/dfmAbbas/dfmReactorDB.svg)](https://jitpack.io/#dfmAbbas/dfmReactorDB)
[![License](http://img.shields.io/badge/license-MIT-green.svg?style=flat)](https://github.com/dfmAbbas/dfmReactorDB)
[![API](https://img.shields.io/badge/API-15%2B-blue.svg?style=flat)](https://github.com/dfmAbbas/dfmReactorDB)

###### ReactorDB is a fast and secure key-value library for Android, and has an embedded database based on the JSON structure and is a great alternative to Shared Performance.
###### This library has a very small size.

## Getting Started :
Add to your root build.gradle :
```Groovy
allprojects {
  repositories {
      maven { url 'https://jitpack.io' } // add this line to repositories
    }
  }
```

Add the dependency :
```Groovy
dependencies {
    implementation 'com.github.dfmAbbas:dfmReactorDB:0.9.0'
}
```

## Simple API (default) :

##### In `Kotlin`:
```Groovy
  //init reactor db
  val reactor = Reactor(applicationContext, "sample_db", SecurityLevel.NONE)

  //insert or update value data by key
  reactor.put("name", "abbas")
  reactor.put("age", 23)
  reactor.put("is_man", true)

  //get value by key
  val name = reactor.getString("name", "none")
  val age = reactor.getInt("age", 0)
  val man = reactor.getBoolean("is_man", false)
```

##### In `Java` :
```Groovy
  //init reactor db
  Reactor reactor = new Reactor(this.getContext(), "sample_db", SecurityLevel.NONE);

  //insert or update value data by key
  reactor.put("name", "abbas");
  reactor.put("age", 23);
  reactor.put("is_man", true);

  //get value by key
  String name = reactor.getString("name", "none");
  int age = reactor.getInt("age", 0);
  boolean man = reactor.getBoolean("is_man", false);
```

## Advanced API :

##### In `Kotlin`:
###### [Sample code written with Kotlin](sample/src/main/java/com/dfmabbas/sample/KotlinSample.kt).

##### In `Java`:
###### [Sample code written with Java](sample/src/main/java/com/dfmabbas/sample/JavaSample.java).

## License
    MIT License

    Copyright (c) 2018 Abbas Naghdi (@dfmAbbas)

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

