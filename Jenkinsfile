pipeline {
    agent { label 'big' }

    options {
        timeout(time: 1, unit: 'HOURS') 
    }

    stages {
        stage('Updating submodules') {
            steps{
                sh 'git submodule update --init'
            }
        }

        stage('Building MiLib from submodule') {
            agent { docker 'maven:3.3.9-jdk-8-alpine' }
            steps {
                dir ('milib') {
                    sh 'mvn -B clean install -DskipTests'
                }
            }
        }
    }
}