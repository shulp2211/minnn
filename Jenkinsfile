pipeline {
    agent any

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
            steps {
                dir ('milib') {
                    sh 'mvn -B clean install -DskipTests'
                }
            }
        }
    }
}