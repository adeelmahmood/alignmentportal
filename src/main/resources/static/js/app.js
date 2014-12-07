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
		.when('/variantsets', {
			templateUrl: '/partials/variantsets.html',
			controller: 'VariantSetsController'
		})
		.when('/reads/:readGroupSetId', {
			templateUrl: '/partials/viewreads.html',
			controller: 'ViewReadsController'
		})
		.when('/variants/:variantSetId', {
			templateUrl: '/partials/viewvariants.html',
			controller: 'ViewVariantsController'
		})
		.otherwise({
				templateUrl: '/partials/home.html',
				controller: 'HomeController'
		});
		
	
});