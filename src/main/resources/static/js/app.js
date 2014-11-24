var alignmentportal = angular.module('alignmentportal', ['ngResource', 'ngRoute', 'angularFileUpload']);

alignmentportal.config(function($routeProvider) {
	
	$routeProvider
		.when('/genomics/datasets', {
			templateUrl: '/partials/genomics/datasets.html',
			controller: 'DatasetsController'
		})
		.when('/genomics/reads', {
			templateUrl: '/partials/genomics/reads.html',
			controller: 'ReadsController'
		})
		.otherwise({
				templateUrl: '/partials/home.html',
				controller: 'HomeController'
		});
		
	
});