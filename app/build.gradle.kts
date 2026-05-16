plugins {
	id 'com.android.application'
}

android {
	compileSdk 34
	
	defaultConfig {
		applicationId "com.maplays.flappybird"
		namespace "com.maplays.flappybird"
		minSdkVersion 21
		targetSdkVersion 34
		versionCode 4
		versionName "1.3"
	}
	
	buildTypes {
		release {
			minifyEnabled false
			proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
		}
	}
	buildFeatures {
		viewBinding false
	}
}

dependencies {
	implementation fileTree(dir: 'libs', include: ['*.jar'])
}
