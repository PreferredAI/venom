build:
	mvn package

travis-deploy:
	gpg --import path/to/private-key.gpg
	mvn clean deploy -P release --settings /path/to/settings.xml