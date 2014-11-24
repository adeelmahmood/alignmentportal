alignmentportal
.controller('HomeController', function($scope, $location, $resource) {

})
.controller('DatasetsController', function($scope, $location, $resource) {
	$scope.dataset = {};
	$scope.newDataset = "";
	
	//retrieve datasets listing
	$scope.datasetsList = $resource('/datasets', {isArray:false});
	$scope.refreshDatasets = function(){ 
		$scope.datasets = $scope.datasetsList.query(function(data){
			if(data && data.length) {
				$scope.dataset = data[0];
			}
		});
	};
	$scope.refreshDatasets();
	
	//create new dataset
	$scope.createNewDatasetReq = $resource('/datasets/create');
	$scope.createNewDataset = function() {
		//send the request to create new dataset
		$scope.createNewDatasetReq.save({dataset:$scope.newDataset}, {}, function(data) {
			$scope.dataset = data;
			$scope.goToReads();
		})
	};
	
	//delete selected dataset
	$scope.deleteDatasetReq = $resource('/datasets/delete');
	$scope.deleteDataset = function() {
		//send the request to delete dataset
		$scope.deleteDatasetReq.save({dataset:$scope.dataset.id}, {}, function(data) {
			$scope.refreshDatasets();
		});
	};
	
	//choose selected dataset
	$scope.chooseDataset = function() {
		$scope.goToReads();
	};
	
	//dataset has been selected go to next page
	$scope.goToReads = function() {
		$location.path('/genomics/reads');
	};
})
.controller('ReadsController', function($scope, $location, $resource, $upload) {

	$scope.uploadSequences = function() {
		var formData = new FormData();
		console.log(formData);
	};
})
;
