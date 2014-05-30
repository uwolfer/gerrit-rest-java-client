gerrit-rest-java-client [![Build Status](https://travis-ci.org/uwolfer/gerrit-rest-java-client.svg?branch=master)](https://travis-ci.org/uwolfer/gerrit-rest-java-client)
======================

Introduction
-----------

Java implementation of the [Gerrit Code Review Tool] REST API.

Only Gerrit 2.6 or newer is supported (missing / incomplete REST API in older versions).

This implementation is used for example as base for the [Gerrit IntelliJ Plugin].

[Gerrit Code Review Tool]: http://code.google.com/p/gerrit/
[Gerrit IntelliJ Plugin]: https://github.com/uwolfer/gerrit-intellij-plugin


Usage
-------
This library implements <code>[com.google.gerrit.extensions.api.GerritApi]</code>. 

You just need a few lines to get it working:
```java
GerritRestApiFactory gerritRestApiFactory = new GerritRestApiFactory();
GerritAuthData.Basic authData = new GerritAuthData.Basic("http://localhost:8080");
// or: authData = new GerritAuthData.Basic("https://example.com/gerrit, "user", "password"");
GerritApi gerritApi = gerritRestApiFactory.create(authData);
List<ChangeInfo> changes = gerritApi.changes().query("status:merged").withLimit(10).get();
```

_Note:_ It is not guaranteed that all interfaces are implemented. If an implementation is missing, you get a
<code>com.google.gerrit.extensions.restapi.NotImplementedException.NotImplementedException</code>. Feel free to
implement it and create a pull request at GitHub - it is quite easy! :)

[com.google.gerrit.extensions.api.GerritApi]: https://gerrit.googlesource.com/gerrit/+/HEAD/gerrit-extension-api/src/main/java/com/google/gerrit/extensions/api/GerritApi.java


Your Support
------------
If you like this plugin, you can support it:
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

Copyright 2013 - 2014 Urs Wolfer

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
