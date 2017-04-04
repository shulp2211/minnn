import groovy.json.JsonOutput

class GlobalData {
   static String gitCommit
}

def githubStatus(state, description) {
    def requestBody = [state: state,
        target_url: "${BUILD_URL}",
        description: description]
    withCredentials([[$class: 'StringBinding', credentialsId: '07141fd2-7f50-4716-8ab5-e1e459ba64da', variable:'githubToken']]) {
        def response = httpRequest httpMode: 'POST', requestBody: JsonOutput.toJson(requestBody), customHeaders: [[name: 'Authorization', value: "token ${githubToken}"]], url: "https://api.github.com/repos/milaboratory/mist/statuses/${GlobalData.gitCommit}"
    }
}

def telegram(status) {
    withCredentials([[$class: 'StringBinding', credentialsId: 'bf16e7f0-fc30-4d33-be60-3c3968392f76', variable:'telegramToken']]) {
        def requestBody = [chat_id:-213623115, text: "[Build](${BUILD_URL}) ${status}", parse_mode: 'Markdown']
        echo JsonOutput.toJson(requestBody)
        def response = httpRequest httpMode: 'POST', contentType: 'APPLICATION_JSON', requestBody: JsonOutput.toJson(requestBody), url: "https://api.telegram.org/bot${telegramToken}/sendMessage"
    }
}

try {
    node('big'){
        properties([pipelineTriggers([[$class: 'GitHubPushTrigger'], pollSCM('H H/8 * * *')])])

        def mvnOpts="-Dmaven.repo.local=${env.WORKSPACE}@tmp/.m2"

        def buildEnvironment = docker.build('mi-build-maven')

        buildEnvironment.inside {
            stage('Updating submodules') {
                checkout scm
                sh 'git submodule update --init'
                GlobalData.gitCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                githubStatus('pending', 'The build is pending!')
            }

            stage('Building MiLib from submodule') {
                sh "cd milib && mvn -B clean install -DskipTests ${mvnOpts}"
            }

            stage('Building MIST and collecting test results') {
                sh "mvn -B clean install ${mvnOpts} || true"
                junit 'target/surefire-reports/*.xml'
            }

            githubStatus('success', 'The build has succeeded!')
            telegram("succeeded.")
        }
    }
} catch (Exception e) {
    githubStatus('failure', 'The build has failed!')
    telegram("failed.")
    throw e
}