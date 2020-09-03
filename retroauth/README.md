# A simple way of calling authenticated requests using retrofit
[![Build Status](https://app.bitrise.io/app/333d6e2bdd7d7306/status.svg?token=XiPwuCStRxgZFLtYToFKTg&branch=master)](https://app.bitrise.io/app/333d6e2bdd7d7306)
## Dependencies
* [Retrofit](https://github.com/square/retrofit) 2.5.0

## Example:
This is how you would create an authenticated call using retroauth. Just create
the interface as you're used to and annotate your authenticated methods as such.
using the ```@Authenticated``` annotation.
``` kotlin
interface SomeService {
    @Authenticated
    @GET("/some/path")
    Call<ResultObject> someAuthenticatedRequest()
}

```

If you're an Android Developer feel free to go directly to the [android implementation](retroauth-android/).
## How to use it?

Add it as dependency:
```groovy
implementation 'com.andretietz.retroauth:retroauth:x.y.z'
```

## The API

This library is made for a system that CAN have multiple users.
These users CAN have multiple different ```TOKEN```s of a specific ```TOKEN_TYPE```.
A User is an ```OWNER``` of a ```TOKEN```, so within the library they're called ```OWNER```.
You can also have different ```OWNER_TYPE```s.

 * ```OWNER_TYPE``` -> contains one or more:
   * ```OWNER```s -> owns one or more:
     * ```TOKEN_TYPE```s -> is bound to exactly one ```TOKEN```

In most of the cases you probably need only one ```OWNER_TYPE``` which contains one ```OWNER```, which owns one ```TOKEN``` of a specific ```TOKEN_TYPE```.
Which is totally fine.


The API provides 3 interaces and an abstract class. All of the
### The interfaces
  * [OwnerManager](src/main/java/com/andretietz/retroauth/OwnerManager.kt): In order to handle one or more Owners on a system you need to provide some basic functionalities to handle this Owners.
  * [TokenStorage](src/main/java/com/andretietz/retroauth/TokenStorage.kt): So that
  * [MethodCache](src/main/java/com/andretietz/retroauth/MethodCache.kt):
  This is an interface optionally to implement. If you don't, you can use it's default implementation, the ```DefaultMethodCache```.
  * [Authenticator](src/main/java/com/andretietz/retroauth/Authenticator.kt): This is an abstract class and it's supposed to be an abstract class to the backend you're authenticating against.

## Pull requests are welcome
Since I am the only one working on that, I would like to know your opinion and/or your suggestions.
Please feel free to create Pull-Requests!

## LICENSE
```
Copyrights 2018 André Tietz

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```