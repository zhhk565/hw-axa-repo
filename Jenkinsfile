node {
    stage 'Checkout from SCM'
    checkout scm

    stage 'Build'

    try {
	echo "In Build Stage ..."
        
    } catch (e) {
        //sh e.toString()
        //throw e
    }

   stage 'Publish'
	echo "Publishing ..."

  stage 'Deployment'
	echo "Deploying  ..."
    }
}

