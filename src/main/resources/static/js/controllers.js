alignmentportal
.controller('HomeController', function($scope, $location, $resource, $routeParams) {
	$scope.datasetId = $routeParams.datasetId;

	$scope.startAlignmentProcess = function() {
		if($scope.datasetId) {
			$scope.startJobQuery = $resource('/jobs/start/:dataset', {isArray:false});
			$scope.startJob = $scope.startJobQuery.query({dataset: $scope.datasetId}, function(data) {
				$location.path('/history/' + $scope.datasetId);				
			});
		}
	};
})
.controller('HistoryController', function($scope, $location, $resource, $routeParams, $interval) {
	$scope.datasets = [];
	//queries for datasets and jobs
	$scope.datasetsList = $resource('/jobs/datasets', {isArray: true});
	$scope.datasetGet = $resource('/datasets/get/:dataset', {isArray:false});
	$scope.jobsList = $resource('/jobs/:dataset', {isArray:false});
	
	//calls to fetch dataset and jobs
	$scope.refreshDatasets = function() {
		$scope.datasetsList.query(function(data) {
			$scope.datasetIds = data;
			//for each dataset id get complete info
			angular.forEach($scope.datasetIds, function(val) {
				$scope.datasetGet.get({dataset: val}, function(data){ 
					$scope.datasets.push({
							id: data.id,
							name: data.name,
							lastRefresh: null,
							jobs: {}
						});
					//start polling jobs
					if($scope.datasets.length == $scope.datasetIds.length) {
						$scope.toggleRefresh();
					}
				});
			});
		});
	};
	$scope.refreshDatasets();
	
	//calls to fetch jobs for each dataset
	$scope.refreshJobs = function() {
		angular.forEach($scope.datasets, function(dataset) {
			$scope.jobsList.query({dataset:dataset.id}, function(data) {
				dataset.jobs = data;
				dataset.lastRefresh = new Date();
			});
		});
	};
	
	$scope.toggleRefreshLabel = "Start Monitoring Progress";
	$scope.toggleRefreshCls = "success";
	$scope.toggleRefresh = function() {
		if(angular.isDefined($scope.refreshInterval)) {
			$interval.cancel($scope.refreshInterval);
			$scope.refreshInterval = undefined;
			$scope.toggleRefreshLabel = "Start Monitoring Progress";
			$scope.toggleRefreshCls = "success";
		}
		else{
			$scope.refreshInterval = $interval($scope.refreshJobs, 2500);
			$scope.toggleRefreshLabel = "Stop Monitoring Progress";
			$scope.toggleRefreshCls = "danger";
		}
	};
	$scope.$on('$destroy', function() {
		if(angular.isDefined($scope.refreshInterval)) {
			$interval.cancel($scope.refreshInterval);
			$scope.refreshInterval = undefined;
		}
	});
	
	$scope.downloadCall = $resource('/download', {}, {
		doIt: {method: 'POST', isArray: false, params: {file:'@file'}}
	});
	$scope.download = function(file) {
		$scope.downloadCall.doIt({file: file.path});
	};
	
	$scope.isInCloud = function(path) {
		return path.match(/gs/) != null;
	};
	$scope.isInProgress = function(file) {
		return file.info && file.info.match(/processing\s/) != null;
	};
	$scope.isInBigquery = function(file) {
		return file.info && file.info.match(/\{apds\:/) != null;
	};
	
	$scope.renderJobInfo = function(info) {
		var str = "";
		var matches = info ? info.match(/([^\{].*)\{([^\:].*):([^\}].*)\}/) : null;
		if(matches != null && matches.length > 0) {
			var orig = matches[1];
			var key = matches[2];
			var val = matches[3];
			if(key == "readgroupsets") {
				str += "<a href='/#/reads/" + val + "' class='btn btn-primary btn-sm'>Explore Reads</a>";
			}
			else if(key == "variantsset") {
				str += "<a href='/#/variants/" + val + "' class='btn btn-primary btn-sm'>Explore Variants</a>";
			}
			else if(key == "apds") {
				str += "<a href='/#/variants/" + val.split("_")[0] + "' class='btn btn-primary btn-sm'>Explore Variants</a>";
			}
		}
		return str ? str : info;
	};
	
	$('[data-toggle="tooltip"]').tooltip();
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
.controller('ReadGroupSetsController', function($scope, $location, $resource, $route, $routeParams) {
	$scope.datasetQuery = $resource('/datasets/get/:dataset', {isArray:false});
	$scope.readGroupSetsList = $resource('/readgroupsets', {isArray:false});
	$scope.readGroupSetsList.query(function(data) {
		$scope.readGroupSets = data;
		//do another query to data dataset detailed information
		angular.forEach($scope.readGroupSets, function(readGroupSet) {
			$scope.datasetQuery.get({dataset:readGroupSet.datasetId}, function(data) {
				readGroupSet.datasetName = data.name;
			});
		});
	});
	
	$scope.viewReads = function(set) {
		$location.path('/reads/' + set.id);		
	};
	
	$scope.deleteQuery = $resource('/readgroupsets/delete/:readGroupSetId', {isArray:false});
	$scope.deleteSet = function(set) {
		if(confirm("Are you sure?")) {
			$scope.deleteQuery.get({readGroupSetId: set.id}, function(data) {
				$route.reload();
			});
		}
	};
})
.controller('VariantSetsController', function($scope, $location, $resource, $route, $routeParams) {
	$scope.datasetQuery = $resource('/datasets/get/:dataset', {isArray:false});
	$scope.variantSetsList = $resource('/variantsets', {isArray:false});
	$scope.variantSetsList.query(function(data) {
		$scope.variantSets = data;
		//do another query to data dataset detailed information
		angular.forEach($scope.variantSets, function(variantSet) {
			$scope.datasetQuery.get({dataset:variantSet.datasetId}, function(data) {
				variantSet.datasetName = data.name;
			});
		});
	});
	
	$scope.viewVariants = function(set) {
		$location.path('/variants/' + set.id);		
	};
	
	$scope.deleteQuery = $resource('/variantsets/delete/:variantSetId', {isArray:false});
	$scope.deleteSet = function(set) {
		if(confirm("Are you sure?")) {
			$scope.deleteQuery.get({variantSetId: set.id}, function(data) {
				$route.reload();
			});
		}
	};
})
.controller('ViewReadsController', function($scope, $location, $resource, $routeParams) {
	$scope.readGroupSetData = {};
	$scope.readsList = $resource('/reads/list/:readGroupSetId', {isArray:false});
	$scope.refreshReads = function() {
		$scope.readsList.get({
					readGroupSetId: $routeParams.readGroupSetId,
					start: $scope.startPosition,
					end: $scope.endPosition,
					referenceName: $scope.referenceName,
					nextPageToken: $scope.readGroupSetData.nextPageToken
				}, function(data) { $scope.readGroupSetData = data; },
				function(err){ alert(err.data.error + "\n" + err.data.message); });	
	};
	$scope.refreshReads();
	
	$scope.getStrand = function(reverse) {
		return reverse ? "-" : "+";
	};
	
	$scope.renderSequence = function(read) {
		var str = "", seqs = "", scores = "";
		for(var i=0; i<read.alignedSequence.length; i++) {
			if(i%30==0 && i>0){
				str += "<div class='text-primary'>" + seqs + "</div><div class='text-muted'>" + scores + "</div>";
				seqs = "";
				scores = "";
			}
			seqs += read.alignedSequence[i] + " ";
			scores += read.alignedQuality.length > i ? read.alignedQuality[i] + " " : "-1 ";
		}
		str += "<div class='text-primary'>" + seqs + "</div><div class='text-muted'>" + scores + "</div>";
		return str;
	};
	$scope.renderTags = function(read) {
		var str = "";
		for(tag in read.info) {
			str += "<div class='text-muted'>" + tag + " : " + read.info[tag].join(" ") + "</div>";
		}
		return str;
	};
	
	$scope.next = function() {
		$scope.refreshReads();
	};
	$scope.first = function() {
		$scope.readGroupSetData.nextPageToken = null;
		$scope.readGroupSetData.previousPageToken = null;
		$scope.refreshReads();
	};
	$scope.search = function() {
		$scope.readGroupSetData.nextPageToken = "";
		$scope.refreshReads();
	};
	$scope.clear = function() {
		$scope.startPosition = "";
		$scope.endPosition = "";
		$scope.referenceName = "";
		$scope.readGroupSetData.nextPageToken = "";
		$scope.refreshReads();
	};
	
	$('[data-toggle="tooltip"]').tooltip();
})
.controller('ViewVariantsController', function($scope, $location, $resource, $routeParams) {
	$scope.variantSetData = {};
	$scope.firstTime = true;
	$scope.variantsList = $resource('/variants/list/:variantSetId', {isArray:false});
	$scope.refreshVariants = function() {
		$scope.variantsList.get({
					variantSetId: $routeParams.variantSetId,
					start: $scope.startPosition,
					end: $scope.endPosition,
					referenceName: $scope.referenceNameObj ? $scope.referenceNameObj.referenceName : null,
					nextPageToken: $scope.variantSetData.nextPageToken
				}, function(data) { 
					$scope.variantSetData = data;
					$scope.referenceNameObj = $scope.findInArr($scope.variantSetData.variantSetReferenceBounds, 
							"referenceName", $scope.variantSetData.variantSetReferenceName);
				},
				function(err){ alert(err.data.error + "\n" + err.data.message); });	
	};
	$scope.refreshVariants();
	
	$scope.findInArr = function(arr, key, val) {
		for(var k in arr) {
			if(arr[k][key] == val) {
				return arr[k];
			}
		}
		return null;
	};
	
	$scope.renderBases = function(bases) {
		if(typeof bases == 'string') {
			return bases;
		}
		else if(typeof bases == 'object') {
			var str = [];
			for(base in bases) {
				str.push(bases[base]);
			}
			return str.join("<br/>");
		} 
	};
	
	$scope.renderInfo = function(info) {
		var str = "";
		for(tag in info) {
			var vals = info[tag].join(",");
			if(vals) {
				str += "<span class='text-primary'>" + tag + "</span> <span class='text-muted'>: " + info[tag].join(",") + "</span><br/>";
			}
		}
		return str;
	};
	
	$scope.next = function() {
		$scope.refreshVariants();
	};
	$scope.first = function() {
		$scope.variantSetData.nextPageToken = null;
		$scope.variantSetData.previousPageToken = null;
		$scope.refreshVariants();
	};
	$scope.search = function() {
		$scope.variantSetData.nextPageToken = "";
		$scope.refreshVariants();
	};
	$scope.clear = function() {
		$scope.startPosition = "";
		$scope.endPosition = "";
		$scope.variantSetData.nextPageToken = "";
		$scope.referenceName = {};
		$scope.refreshVariants();
	};
	
	$('[data-toggle="tooltip"]').tooltip();
}).
filter('unsafe', function($sce) {
	return function(val) {
		return $sce.trustAsHtml(val);
	};
})
;