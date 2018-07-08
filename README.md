## FabAnimator
[![](https://jitpack.io/v/okabbas/FabAnimator.svg)](https://jitpack.io/#dfmAbbas/dfmReactor)
[![License](http://img.shields.io/badge/license-MIT-green.svg?style=flat)](https://github.com/dfmAbbas/dfmReactor)
[![API](https://img.shields.io/badge/API-15%2B-blue.svg?style=flat)](https://github.com/dfmAbbas/dfmReactor)

###### Reactor is a fast and secure key-value database for storing Android app settings and data .



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
    implementation :) developing ...
}
```

## Simple API (default) :

##### In `Kotlin`:
```Groovy
  //init reactor db
  val reactor = Reactor(applicationContext, "UDB", SecurityLevel.NONE)

  //insert or update value data by key
  reactor.put("name", "abbas")
  reactor.put("age", 23)
  reactor.put("is_man", true)

  //get value by key
  val name = reactor.getString("name", "ghazale")
  val age = reactor.getInt("age", 1)
  val man = reactor.getBoolean("is_man", false)
```

##### In `Java` :
```Groovy
```

## Advanced API :

##### In `Kotlin`:
###### [Sample code written with Katlin](Sample/src/main/java/com/github/okabbas/FabAnimator/Sample/KotlinView.kt).

##### In `Java`:
###### [Sample code written with Java](Sample/src/main/java/com/github/okabbas/FabAnimator/Sample/JavaView.java).

## License
    MIT License

    Copyright (c) 2018 Abbas Naghdi

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

