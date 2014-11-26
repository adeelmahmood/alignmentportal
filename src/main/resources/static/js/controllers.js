alignmentportal
.controller('HomeController', function($scope, $location, $resource, $routeParams) {
	$scope.datasetId = $routeParams.datasetId;
})
.controller('HistoryController', function($scope, $location, $resource, $routeParams) {
	//retrieve jobs listing
	$scope.jobsList = $resource('/jobs', {isArray:false});
	$scope.jobsList.query(function(data){
		$scope.jobs = data;
	});
	
	$scope.getFileName = function(filepath) {
		return filepath.substring(filepath.lastIndexOf("\\")+1);
	};	
	$scope.getDate = function(millis) {
		return new Date(millis);
	};
})
.controller('DatasetsController', function($scope, $location, $resource, $routeParams) {
	$scope.dataset = {};
	$scope.newDataset = "";
	$scope.params = $routeParams;
	
	//retrieve datasets listing
	$scope.datasetsList = $resource('/datasets', {isArray:false});
	$scope.refreshDatasets = function(){ 
		$scope.datasets = $scope.datasetsList.query(function(data){
			//iterate thru all datasets to see which one should be selected
			for(var i=0; i<data.length; i++) {
				//if the dataset is already selected
				if(!jQuery.isEmptyObject($scope.dataset) && data[i].id == $scope.dataset.id) {
					$scope.dataset = data[i];
					break;
				}
				//check if dataset has already been selected by scope param
				else if($scope.params.datasetId && data[i].id == $scope.params.datasetId) {
					$scope.dataset = data[i];
					break;
				}
			}
			if(jQuery.isEmptyObject($scope.dataset) && data.length > 0) {
				$scope.dataset = data[0];
			}
		});
	};
	$scope.refreshDatasets();
	
	//create new dataset
	$scope.createNewDatasetReq = $resource('/datasets/create');
	$scope.createNewDataset = function() {
		if(!$scope.newDataset){
			return;
		}
		//send the request to create new dataset
		$scope.createNewDatasetReq.save({dataset:$scope.newDataset}, {}, function(data) {
			$scope.dataset = data;
			$scope.goToReads();
		})
	};
	
	//delete selected dataset
	$scope.deleteDatasetReq = $resource('/datasets/delete');
	$scope.deleteDataset = function() {
		if(!$scope.dataset.id) {
			return;
		}
		//send the request to delete dataset
		$scope.deleteDatasetReq.save({dataset:$scope.dataset.id}, {}, function(data) {
			$scope.dataset = {};
			$scope.refreshDatasets();
		});
	};
	
	//choose selected dataset
	$scope.chooseDataset = function() {
		if($scope.dataset.id) {
			$scope.goToReads();
		}
	};
	
	//dataset has been selected go to next page
	$scope.goToReads = function() {
		$location.path('/genomics/reads/' + $scope.dataset.id);
	};
})
.controller('ReadsController', function($scope, $location, $resource, FileUploader, $routeParams) {
	$scope.refGenomeFile = {};
	$scope.refGenomeFileDone = false;
	$scope.sampleSeqFile = {};
	$scope.sampleSeqFileDone = false;
	$scope.params = $routeParams;
	$scope.uploader = new FileUploader({
		url: '/upload'
	});

	 $scope.uploader.onBeforeUploadItem = function(item) {
//		 Array.prototype.push.apply(item.formData, $scope.uploader.formData);
		 item.formData.push({dataset: $scope.params.datasetId, fileType: item.fileType});
		 if(item.fileType == 'refGenome') {
			 $scope.refGenomeFile = item;
		 }
		 else if(item.fileType == 'sampleSeq') {
			 $scope.sampleSeqFile = item;
		 }
	 };
	 $scope.uploader.onSuccessItem = function(item, response, status, headers) {
		 if(item.fileType == 'refGenome') {
			 $scope.refGenomeFileDone = true;
		 }
		 else if(item.fileType == 'sampleSeq') {
			 $scope.sampleSeqFileDone = true;
		 }
		 //if both files are done
		 if($scope.refGenomeFileDone && $scope.sampleSeqFileDone) {
			 $scope.goToDone();
		 }
	 };
	 $scope.uploader.onErrorItem = function(item, response, status, headers) {
		 if(item.fileType == 'refGenome') {
			 $scope.refGenomeFileDone = false;
		 }
		 else if(item.fileType == 'sampleSeq') {
			 $scope.sampleSeqFileDone = false;
		 }
	 }; 
     $scope.uploader.onCancelItem = function(item, response, status, headers) {
    	 if(item.fileType == 'refGenome') {
			 $scope.refGenomeFileDone = false;
		 }
		 else if(item.fileType == 'sampleSeq') {
			 $scope.sampleSeqFileDone = false;
		 }
     };
     
	$scope.backToDatasets = function() {
		$location.path('/genomics/datasets/' + $scope.params.datasetId);
	};
	
	$scope.goToDone = function() {
		$location.path('/genomics/done/' + $scope.params.datasetId);
	};
	
	$scope.uploadSequences = function() {
		$scope.uploader.uploadAll();
	}
})
;