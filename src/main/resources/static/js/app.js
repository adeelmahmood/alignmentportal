var alignmentportal = angular.module('alignmentportal', ['ngResource', 'ngRoute', 'angularFileUpload']);

alignmentportal.config(function($routeProvider) {
	
	$routeProvider
		.when('/genomics/datasets/:datasetId?', {
			templateUrl: '/partials/genomics/datasets.html',
			controller: 'DatasetsController'
		})
		.when('/genomics/reads/:datasetId', {
			templateUrl: '/partials/genomics/reads.html',
			controller: 'ReadsController'
		})
		.when('/genomics/done/:datasetId', {
			templateUrl: '/partials/genomics/done.html',
			controller: 'HomeController'
		})
		.when('/history/:datasetId?', {
			templateUrl: '/partials/history.html',
			controller: 'HistoryController'
		})
		.when('/readgroupsets', {
			templateUrl: '/partials/readgroupsets.html',
			controller: 'ReadGroupSetsController'
		})
		.when('/reads/:readGroupSetId', {
			templateUrl: '/partials/viewreads.html',
			controller: 'ViewReadsController'
		})
		.otherwise({
				templateUrl: '/partials/home.html',
				controller: 'HomeController'
		});
		
	
});