# A distributed, plug-n-play algorithm for multi-robot applications

This project is a multi-robot framework capable of dealing with applications where the overall mission objectives can be casted to the optimization of an objective function. More information about the algorithm and an extensive set of experiments can be found [here](http://kapoutsis.info/wp-content/uploads/2019/05/j4.pdf). 

 Inside the project there are two different multi-robot frameworks: 
 
[**HoldTheLine**]: A simple problem (toy-problem), where the robots should be deployed in a specific formation (line). 

[**AdaptiveCoverage2D**]: In the second simulation setup the objective is to spread out the robots over a 2D environment while aggregating in areas of high sensory interest. An important aspect of the set-up is the fact that, the robots are not aware beforehand of the sensory areas of interest, but they learn this information on-line, utilizing sensor measurements from their current positions.

## Built With

* [IntelliJ IDEA](https://www.jetbrains.com/idea/) - The used java IDEA 
* [Maven](https://maven.apache.org/) - Dependency Management


## Add your multi-robot testbed

A valid testbed should contain the following:

-  A class inside the package *testbed.TESTBED_NAME* with name 
	```
	Framework
	``` 
	 that extends the *environment.Setup* class.
	 

-  A properties file inside *resources.testbeds.TESTBED_NAME* with name
	```
	Parameters.properties
	```
	that contains all the needed fields (check *Parameters_template.properties* file). 

> **Note:** Examples of implemented testbeds along with the corresponing functions can be found inside the *testbeds.AdaptiveCoverage2D* and  *testbeds.HoldTheLine* packages.

## Run the tests

The *validator* class contains tests for each one of the implemented testbeds. In each one of these tests, a series of experiments is executed with different number of robots. 

Any new testbed should be acompnied with the appropiate series of unit tests that documents its expected operation/performance.


## Authors

* **Dr. Athanasios Kapoutsis**  - [Personal Site](http://kapoutsis.info/)

## License

This project is licensed under the GNU GPLv3 License - see the [LICENSE.md](LICENSE.md) file for details

## Cite as: 

```
@article{kapoutsis2019distributed,
title={A distributed, plug-n-play algorithm for multi-robot applications with a priori non-computable objective functions},
author={Kapoutsis, Athanasios Ch and Chatzichristofis, Savvas A and Kosmatopoulos, Elias B},
journal={The International Journal of Robotics Research},
year={2019},
volume={38},
number={7},
pages={813-832},
issn={1573-0409},
doi={10.1177/0278364919845054},
publisher={SAGE}
}
```
