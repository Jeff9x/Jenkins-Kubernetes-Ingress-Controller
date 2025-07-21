pipeline {
    // Use a basic agent for the initial checkout
    agent any

    // Environment variables available to all stages
    environment {
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials')
        DOCKER_HUB_USERNAME = 'jeff9x' // Your Docker Hub username
        KUBECONFIG_CREDENTIALS_ID = 'kubeconfig'
    }

    stages {
        // Stage 1: Checkout source code from Git
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        // Stage 2: Build and push the first application
        stage('Build & Push App One') {
            // Use a Docker container as the agent for this specific stage.
            // This provides the 'docker' command.
            agent {
                docker { image 'docker:24.0-git' }
            }
            steps {
                script {
                    // Use the Docker Pipeline plugin for cleaner credential handling
                    docker.withRegistry('https://index.docker.io/v1/', DOCKER_HUB_CREDENTIALS) {
                        def appImage = docker.build("${DOCKER_HUB_USERNAME}/app-one:latest", './app-one')
                        appImage.push()
                    }
                }
            }
        }

        // Stage 3: Build and push the second application
        stage('Build & Push App Two') {
            agent {
                docker { image 'docker:24.0-git' }
            }
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKER_HUB_CREDENTIALS) {
                        def appImage = docker.build("${DOCKER_HUB_USERNAME}/app-two:latest", './app-two')
                        appImage.push()
                    }
                }
            }
        }

        // Stage 4: Build and push the third application
        stage('Build & Push App Three') {
            agent {
                docker { image 'docker:24.0-git' }
            }
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKER_HUB_CREDENTIALS) {
                        def appImage = docker.build("${DOCKER_HUB_USERNAME}/app-three:latest", './app-three')
                        appImage.push()
                    }
                }
            }
        }

        // Stage 5: Deploy all applications and the Ingress controller to Kubernetes
        stage('Deploy All Apps & Ingress') {
            steps {
                script {
                    // Use withCredentials to securely handle the kubeconfig file
                    withCredentials([kubeconfigContent(credentialsId: KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG_CONTENT')]) {
                        sh '''
                            #!/bin/bash
                            # Write the kubeconfig content to a temporary file
                            echo "$KUBECONFIG_CONTENT" > kubeconfig
                            export KUBECONFIG=./kubeconfig

                            # Apply Kubernetes manifests
                            kubectl apply -f ./app-one/deployment.yaml
                            kubectl apply -f ./app-two/deployment.yaml
                            kubectl apply -f ./app-three/deployment.yaml
                            kubectl apply -f ./ingress/ingress.yaml
                        '''
                    }
                }
            }
        }

        // Stage 6: Verify the deployments in Kubernetes
        stage('Verify All Deployments') {
            steps {
                script {
                    withCredentials([kubeconfigContent(credentialsId: KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG_CONTENT')]) {
                        sh '''
                            #!/bin/bash
                            echo "$KUBECONFIG_CONTENT" > kubeconfig
                            export KUBECONFIG=./kubeconfig

                            # Add a small delay to allow resources to be created
                            echo "Waiting for deployments to be ready..."
                            sleep 15 

                            # Verify the status of Kubernetes resources
                            kubectl get deployments
                            kubectl get services
                            kubectl get ingress
                        '''
                    }
                }
            }
        }
    }
}
