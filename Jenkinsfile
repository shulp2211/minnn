node('big'){
    stage('Updating submodules') {
        sh 'git submodule update --init'
    }

    stage('Building MiLib from submodule') {
        docker.build('mi-build-maven:1.0').inside {
            sh 'cd milib && mvn -B clean install -DskipTests'
        }
    }
}