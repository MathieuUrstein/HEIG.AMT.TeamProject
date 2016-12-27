# HEIG.AMT.Gamification

A gamification platform for aplications that want to attribute badges and points accordingly to rules specified. 

Applications can register to the platform and create rules, badges and point scales for their users. When there is an event notified to the platform related to an user (for example a message posted), his state is modified and accordingly to the rules established, he can gain point or badges.

This project was made with [Spring Boot](https://projects.spring.io/spring-boot/) in the AMT course at HEIG-VD. 

Note that this is still a work in progress.

# Deployment

To deploy our app, you will need the following:
- Docker version 1.12.3 and docker-compose
- Apache Maven 3.3.9

*Warning: Before anything, you may need to stop any service running on port 3306 and 8080.*

1. Clone the repo and cd into it.
2. `$ ./deploy.sh`
3. That's it, the app should be listening at [http://localhost:8080/](http://localhost:8080/). Of course, 
if you don't run docker directly on your system (for example on a vm), the host should be the adress of the docker host and not `localhost`.

If for any reason you prefer to do it manually instead of running the script, you can do the following:

1. `$ mkdir -p images/maven/gamification`
2. `$ cp -r src/ images/maven/gamification/`
3. `$ cp pom.xml images/maven/gamification/`
4. `$ docker-compose up --build`

# Development

If you want to work on the project, you can of course edit files and re-run `deploy.sh` each time, but 
that's a bit long since you have to restart the dockers each time and you can't run the project directly 
from your IDE (because the host in the url of the DB connexion is an alias defined in the docker-compose).

What you could want is run only the mysql docker and run the project in your IDE. In order to do that, 
you will need to do the following:

1. `$ docker-compose up --build mysql` to run only the mysql container.
2. create a [configuration file](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-profile-specific-properties)
in `/src/main/resources/` named `application-default.properties`. You can name it differently (`application-{profile}.properties`),
but you will have to specify later the profile used when you run the project.
3. Inside, put the following line: `spring.datasource.url=jdbc:mysql://<host for the docker>:3306/gamification?useSSL=false`.
4. You can now run the project from your IDE.

Note: the basic behavior of the server is to keep the values registered in the DB. This can be changed by 
adding the `spring.jpa.hibernate.ddl-auto` parameter in your `application-default.properties` and set it
to wanted behavior (check the [Hibernate documentation](https://docs.jboss.org/hibernate/orm/5.2/userguide/html_single/Hibernate_User_Guide.html#configurations-hbmddl) 
to see the possible values).


# Tests

Unit tests are available to test the application and are run by travis. If you want to run them yourself, here are the steps needed to do it.
Requirements: 
- Python 3
- libmysqlclient-dev (on linux)
- You may also want to setup a virtual environment based on python 3 (else, be careful if you have multiple python versions to not use the wrong python or pip).

1. Create a test.conf file in the root of the project. You can copy-paste the test_default.conf.
2. Edit it and specify the correct values:
  * For the mysql DB, the host, port and the password used (if you use the one coming with the docker-compose, set `password = root`).
  * For the gamification app, the host and port used.
3. At the root of the project, run `python -m unittest discover tests`.
4. The result of the unit tests are displayed.


# Authors

Made by [Sébastien Boson](https://github.com/sebastie-boson), 
[Benjamin Schubert](https://github.com/BenjaminSchubert), 
[Mathieu Urstein](https://github.com/MathieuUrstein) and 
[Basile Vu](https://github.com/Flagoul).
