<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at
  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
Apache Tamaya Resources Module
------------------------------

The Apache Tamaya resources module provides an additional service called 'ResourceLoader', which is accessible
from the  ServiceContext. The new service allows resolution of resources (modelled as URL) using Ant  styled
patterns:

* ? may represent any character (but there must be one)
* * may represent any character in the path (can be none or multiple)
* ** may be used to let the pattern matcher go down the hierarchy of files od resources in the current locations.

The resolver supports by default resolving paths in the file system and within the classpath, e.g.

  resources_testRoot/**/*.file
  c:\temp\**\*

In case of a conflict the resolver mechanism can also be explicitly addressed by adding the regarding prefix, so
the above expressions above are equivalent to

  classpath:resources_testRoot/**/*.file
  file:c:\temp\**\*

Most benefits are created, when also using the formats module, which provides an implementation of a 
PropertySourceProvider taking a setCurrent of paths to be resolved and a number of supported formats.


