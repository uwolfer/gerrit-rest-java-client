gerrit-rest-java-client
======================

[![Linux Build](https://travis-ci.org/uwolfer/gerrit-rest-java-client.svg?branch=master)](https://travis-ci.org/uwolfer/gerrit-rest-java-client)
[![Windows Build](https://ci.appveyor.com/api/projects/status/ctm64o74lxdri26s/branch/master?svg=true)](https://ci.appveyor.com/project/uwolfer/gerrit-rest-java-client/branch/master)
[![Coverage Status](https://img.shields.io/coveralls/uwolfer/gerrit-rest-java-client.svg)](https://coveralls.io/r/uwolfer/gerrit-rest-java-client)
[![Dependency Status](https://www.versioneye.com/java/com.urswolfer.gerrit.client.rest:gerrit-rest-java-client/badge)](https://www.versioneye.com/java/com.urswolfer.gerrit.client.rest:gerrit-rest-java-client)

Introduction
-----------

Java implementation of the [Gerrit Code Review Tool] REST API.

Only Gerrit 2.6 or newer is supported (missing / incomplete REST API in older versions).

This implementation is used for example as base for the [Gerrit IntelliJ Plugin].

Many different authentication-methods are supported (HTTP basic, HTTP digest, LDAP with form,
HTTP password from Gerrit setting, ...).

[Gerrit Code Review Tool]: http://code.google.com/p/gerrit/
[Gerrit IntelliJ Plugin]: https://github.com/uwolfer/gerrit-intellij-plugin


Usage
-------
This library implements <code>[com.google.gerrit.extensions.api.GerritApi]</code>.

You just need a few lines to get it working:
```java
GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
GerritAuthData.Basic authData = new GerritAuthData.Basic("http://localhost:8080");
// or: authData = new GerritAuthData.Basic("https://example.com/gerrit", "user", "password"");
GerritApi gerritApi = gerritRestApiFactory.create(authData);
List<ChangeInfo> changes = gerritApi.changes().query("status:merged").withLimit(10).get();
```

If you like to write a script instead of a full Java application, you might want to use [Groovy].
There is a [basic Groovy example] available.

_Note:_ It is not guaranteed that all interfaces are implemented. If an implementation is missing, you get a
<code>com.google.gerrit.extensions.restapi.NotImplementedException</code>. Feel free to implement it and create a pull
request at GitHub - it is quite easy! :)

_Note:_ The source of <code>com.google.gerrit.extensions</code> is included in this repository at the
moment because not all extensions to this API are merged into Gerrit repository yet.

[com.google.gerrit.extensions.api.GerritApi]: https://gerrit.googlesource.com/gerrit/+/HEAD/gerrit-extension-api/src/main/java/com/google/gerrit/extensions/api/GerritApi.java
[Groovy]: http://www.groovy-lang.org/
[basic Groovy example]: https://github.com/uwolfer/gerrit-rest-java-client/blob/master/examples/Basic.groovy

Maven Artifact
--------------
Releases are available with Maven:
```xml
<dependency>
    <groupId>com.urswolfer.gerrit.client.rest</groupId>
    <artifactId>gerrit-rest-java-client</artifactId>
    <version>0.8.11</version>
</dependency>
```

Android Support
---------------
Apache HttpClient causes problems on Android platform. There is a workaround by using [HttpClient for Android].
Android support builds are not officially released, but you should be able to create your own build by using the
[httpclient-android branch]. You probably want to merge master branch into this branch before building it.

[HttpClient for Android]: https://hc.apache.org/httpcomponents-client-4.3.x/android-port.html
[httpclient-android branch]: https://github.com/uwolfer/gerrit-rest-java-client/tree/httpclient-android

Dependencies
------------
This library depends on [Apache HttpClient], [Gson] and [Guava].

[Apache HttpClient]: https://hc.apache.org/httpcomponents-client-ga/
[Gson]: https://github.com/google/gson
[Guava]: https://github.com/google/guava

Your Support
------------
If you like this library, you can support it:
* Star it: [Star it at GitHub]. GitHub account required.
* Improve it: Report bugs or feature requests. Or even fix / implement them by yourself - everything is open source!
* Donate: You can find donation-possibilities at the bottom of this file.
[Star it at GitHub]: https://github.com/uwolfer/gerrit-rest-java-client


Donations
--------
If you like this work, you can support it with [this donation link]. If you don't like Paypal
(Paypal takes 2.9% plus $0.30 per transaction fee from your donation), please contact me.
Please only use the link from github.com/uwolfer/gerrit-intellij-plugin to verify that it is correct.
[this donation link]: https://www.paypal.com/webscr?cmd=_s-xclick&hosted_button_id=8F2GZVBCVEDUQ


Copyright and license
--------------------

Copyright 2013 - 2016 Urs Wolfer

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
