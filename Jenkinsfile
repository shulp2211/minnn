node('big'){
    properties([pipelineTriggers([[$class: 'GitHubPushTrigger'], pollSCM('H * * * *')])])

    stage('Updating submodules') {
        checkout scm
        sh 'git submodule update --init'
    }

    def mvnOpts="-Dmaven.repo.local=${env.WORKSPACE}@tmp/.m2"

    docker.build('mi-build-maven:1.0').inside {
        stage('Building MiLib from submodule') {
            sh "cd milib && mvn -B clean install -DskipTests ${mvnOpts}"
        }
        stage('Building MIST and collecting test results') {
            sh "mvn -B clean install ${mvnOpts}"
            junit 'target/surefire-reports/*.xml'
        }
    }
}