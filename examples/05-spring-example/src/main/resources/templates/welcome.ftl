<!DOCTYPE html>
<!--
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy current the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
-->
<html lang="en">

<body>
    <h1>Tamaya - Spring Boot Example</h1>
    This example shows how Tamaya Configuration can be used with Spring Boot to enable Spring with Tamaya
    dynamic configuration features and Tamaya Configuration Injection.
    <h2>Accessing properties programmatically</h2>
    Configuration properties can be easily accessed with Tamaya's Java API:<br/>
<pre>
Configuration config = ConfigurationProvider.getConfiguration();
String value = config.get("foreground.color");
</pre>
   Hereby Tamaya also offers type safe access:<br/>
<pre>
Color color = config.get("foreground.color", Color.class);
</pre>
    <h2>Annotating properties</h2>
    Configuration properties on beans can be easily annotated with Tamaya.
    <ul>
    <li>You can use default Spring mechanism, e.g.:<br/>
<pre>
@Value("$\{application.message\:Hello World}")
private String message = "Hello World";
</pre></li>
    <li>You can use the Tamaya injection API, e.g.<br/>
<pre>
@Config(value = "background.color", required = false)
private String backgroundColor = "#BBBBBB";
</pre></li>
    <li>You can also use Tamaya's dynmic configuration features:<br/>
<pre>
@Config(value = "foreground.color", required = false, defaultValue = "#DDDDDD")
private DynamicValue<String> foregroundColor;
</pre>
    </li>
    <li>As with the programmatic API, type safe configuration is supported similarly:<br/>
<pre>
@Config(value = "background.color", required = false)<
<b>private</b> Color bgColor = "#BBBBBB";
</pre></li>
    </ul>
    <h2>Demo: setting the foreground color</h2>
    This small form allows you to set the foreground color of the following box. For simplicity it
    uses standard system property, but configuration could also be accessed from any kind of remote or local
    resource:
    <p/>
    <form action="/update">
       <input type="text" name="foreground" value="${foreground}"/>
       <input type="submit" name="action-update"/>
    </form>
    <br/>
    </p>
    <table>
    <tr><td>
    <p>
    Foreground Color: ${foreground}<br/>
    Background Color: ${background}</p>
    </td>
    <td bgcolor="${background}">
    <font color="${foreground}">
	Date: ${time?date}
	<br/>
	Time: ${time?time}
	<br/>
	Message: ${message}
	</font>
	</p>
	</td></tr></table>
	<h2>Configuration Data</h2>
	Cou can access the current configuration using the following URL: <a href="/config">/config</a>. <br/>
	Add	additional path parameter for filtering...
</body>

</html>