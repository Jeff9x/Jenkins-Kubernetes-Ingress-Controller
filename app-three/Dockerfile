# Use the official Long-Term Support (LTS) Jenkins image as a base
FROM jenkins/jenkins:lts-jdk17

# Switch to the root user to install packages
USER root

# Install the Docker CLI
RUN apt-get update && apt-get install -y lsb-release
RUN curl -fsSLo /usr/share/keyrings/docker-archive-keyring.asc https://download.docker.com/linux/debian/gpg
RUN echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.asc] https://download.docker.com/linux/debian $(lsb_release -cs) stable" > /etc/apt/sources.list.d/docker.list
RUN apt-get update && apt-get install -y docker-ce-cli

# Create the docker group and add the 'jenkins' user to it
RUN groupadd docker && usermod -aG docker jenkins

# Switch back to the jenkins user
USER jenkins
