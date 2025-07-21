pipeline {
    agent any
    environment {
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub-credentials')
        DOCKER_HUB_USERNAME = 'jeff9x'
        KUBECONFIG_CREDENTIALS_ID = 'kubeconfig'
    }
    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }
        stage('Build & Push App One') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', 'docker-hub-credentials') {
                        def appImage = docker.build("${env.DOCKER_HUB_USERNAME}/app-one", './app-one')
                        appImage.push('latest')
                    }
                }
            }
        }
        stage('Build & Push App Two') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', 'docker-hub-credentials') {
                        def appImage = docker.build("${env.DOCKER_HUB_USERNAME}/app-two", './app-two')
                        appImage.push('latest')
                    }
                }
            }
        }
        stage('Build & Push App Three') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', 'docker-hub-credentials') {
                        def appImage = docker.build("${env.DOCKER_HUB_USERNAME}/app-three", './app-three')
                        appImage.push('latest')
                    }
                }
            }
        }
        stage('Deploy All Apps & Ingress') {
            steps {
                script {
                    withCredentials([kubeconfigContent(credentialsId: env.KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG_CONTENT')]) {
                        sh '''
                            #!/bin/bash
                            echo "$KUBECONFIG_CONTENT" > kubeconfig
                            export KUBECONFIG=./kubeconfig
                            kubectl apply -f ./app-one/deployment.yaml
                            kubectl apply -f ./app-two/deployment.yaml
                            kubectl apply -f ./app-three/deployment.yaml
                            kubectl apply -f ./ingress/ingress.yaml
                        '''
                    }
                }
            }
        }
        stage('Verify All Deployments') {
            steps {
                script {
                    withCredentials([kubeconfigContent(credentialsId: env.KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG_CONTENT')]) {
                        sh '''
                            #!/bin/bash
                            echo "$KUBECONFIG_CONTENT" > kubeconfig
                            export KUBECONFIG=./kubeconfig
                            kubectl get deployments
                            kubectl get services
                            kubectl get ingress
                        '''
                    }
                }
            }
        }
    } // This brace closes the 'stages' block
}     // This final brace closes the 'pipeline' block
