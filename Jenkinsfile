node {
    stage 'Checkout from SCM'
    checkout scm

    stage 'Build'

    try {
 sh 'sbt clean test package assembly -Dsbt.override.build.repos=true -Dsbt.log.noformat=true'

    } catch (e) {

   // echo "e.toString()"
       
    }

    //def branchName = "${env.BRANCH_NAME}"

    //if (branchName == "master" || branchName == "develop") 
   // echo "Branch name : ${env.BRANCH_NAME}"
    //{
        stage 'Publish package to Nexus'
        sh 'sbt publish -Dsbt.override.build.repos=true -Dsbt.log.noformat=true'

        stage 'Publish deployment package to Nexus'
        sh 'sbt universal:publish -Dsbt.override.build.repos=true -Dsbt.log.noformat=true'
   // }
}
