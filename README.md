cog-tasks
=========

A battery of computerised cognitive tasks that can be used for research purposes.

All files in this repository are part of the cog-tasks project. They provide a set 
of flexible executables to be used in the tatool framework for deploying 
computerised behavioural assessments. 

cog-tasks is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

cog-tasks is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

A copy of v3.0 of the GNU GPL is included in the repository or see, 
<http://www.gnu.org/licenses/>.

The primary working memory tasks in this project are discussed in a published paper:

Stone, J M and Towse, J N 2015 A Working Memory Test Battery: Java-Based Collection of Seven Working Memory Tasks. Journal of Open Research Software 3:e5, DOI: http://dx.doi.org/10.5334/jors.br

If you use these tasks in your research please cite this paper as well as the 
original paper outlining the Tatool project (DOI: 10.3758/s13428-012-0224-y)


This repo has more or less everything you need to setup a maven project to start editing the code, however there additional steps:

1) You will likely need to create a src/test/resources directory.

2) The current code depends on some modifications I made to the tatool-core source code. Therefore you will get some Java errors unless you replace the default tatool-core-1.3.2.jar in the maven repository with the version in the core_replace folder. - This was poorly done on my part and in an updated version in the future I plan on removing the necessity of this step. 


*Updates Incoming*