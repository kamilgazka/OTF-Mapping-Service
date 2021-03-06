'use strict';

angular.module('mapProjectApp.widgets.projectDetails', [ 'adf.provider' ]).config(
  function(dashboardProvider) {
    dashboardProvider.widget('projectDetails', {
      title : 'Project Details',
      description : 'Displays details for a specific map project.',
      templateUrl : 'js/widgets/projectDetails/projectDetails.html',
      controller : 'projectDetailsCtrl',
      resolve : {
        data : function(projectDetailsService, config) {
          if (!config.terminology) {
            return 'SNOMEDCT';
          }
          return config.terminology;
        }
      },
      edit : {}
    });
  }).service('projectDetailsService', function($q, $http) {
  return {
    get : function(terminology) {
      var deferred = $q.defer();
      $http({
        url : root_metadata + 'metadata/terminology/id/' + terminology,
        dataType : 'json',
        method : 'GET',
        headers : {
          'Content-Type' : 'application/json'
        }
      }).success(function(data) {
        if (data) {
          data.terminology = terminology;
          deferred.resolve(data);
        } else {
          deferred.reject();
        }
      }).error(function(data, status, headers, config) {

        $rootScope.handleHttpError(data, status, headers, config);
        deferred.reject();
      });
      return deferred.promise;
    }
  };
})
// filter out users/entities who are already on the selected list
// since this is used to return list of potential entities to add for
// selection
.filter('elementFilter', function() {
  return function(elementList, elementsToCheck) {
    var out = new Array();
    if (elementList == undefined || elementsToCheck == undefined)
      return out;
    for (var i = 0; i < elementList.length; i++) {
      var found = false;
      for (var j = 0; j < elementsToCheck.length; j++) {
        if (elementList[i].name === elementsToCheck[j].name) {
          found = true;
          break;
        }
      }
      if (found == false)
        out.push(elementList[i]);
    }
    return out;
  };
})

.controller(
  'projectDetailsCtrl',
  [
    '$scope',
    '$http',
    '$sce',
    '$rootScope',
    '$location',
    'localStorageService',
    '$q',
    'Upload',
    'utilService',
    function($scope, $http, $sce, $rootScope, $location, localStorageService, $q, Upload,
      utilService) {
      $scope.page = 'project';

      $scope.currentRole = localStorageService.get('currentRole');
      $scope.currentUser = localStorageService.get('currentUser');
      $scope.focusProject = localStorageService.get('focusProject');
      $scope.mapProjects = localStorageService.get('mapProjects');
      $scope.mapUsers = localStorageService.get('mapUsers');

      $scope.focusProjectBeforeChanges = {};
      $scope.focusProjectBeforeChanges = angular.copy($scope.focusProject);

      $scope.editModeEnabled = false;
      $scope.reportDefinitions = new Array();
      $scope.qaCheckDefinitions = new Array();

      $scope.allowableMapTypes = [ {
        displayName : 'Extended Map',
        name : 'ExtendedMap'
      }, {
        displayName : 'Complex Map',
        name : 'ComplexMap'
      }, {
        displayName : 'Simple Map',
        name : 'SimpleMap'
      } ];
      $scope.allowableMapRelationStyles = [ {
        displayName : 'Map Category Style',
        name : 'MAP_CATEGORY_STYLE'
      }, {
        displayName : 'Relationship Style',
        name : 'RELATIONSHIP_STYLE'
      } ];
      $scope.allowableWorkflowTypes = [ {
        displayName : 'Conflict workflow',
        name : 'CONFLICT_PROJECT'
      }, {
        displayName : 'Review workflow',
        name : 'REVIEW_PROJECT'
      }, {
        displayName : 'Simple workflow',
        name : 'SIMPLE_PATH'
      }, {
        displayName : 'Legacy workflow',
        name : 'LEGACY_PATH'
      } ];

      // watch for focus project change
      $scope.$on('localStorageModule.notification.setFocusProject', function(event, parameters) {
        $scope.focusProject = parameters.focusProject;
      });

      $scope.userToken = localStorageService.get('userToken');

      $scope.$watch([ 'focusProject', 'userToken' ], function() {

        if ($scope.focusProject != null && $scope.userToken != null) {
          // n/a
        }
        $http.defaults.headers.common.Authorization = $scope.userToken;
        $scope.go();
      });

      $scope.go = function() {

        console.debug('Formatting project details');
        $http({
          url : root_mapping + 'advice/advices',
          dataType : 'json',
          method : 'GET',
          headers : {
            'Content-Type' : 'application/json'
          }
        }).success(function(data) {
          console.debug('  advices = ', data);
          $scope.mapAdvices = data.mapAdvice;
          localStorageService.add('mapAdvices', data.mapAdvice);
          $rootScope.$broadcast('localStorageModule.notification.setMapAdvices', {
            key : 'mapAdvices',
            mapAdvices : data.mapAdvices
          });
          $scope.allowableMapAdvices = localStorageService.get('mapAdvices');
        }).error(function(data, status, headers, config) {
          $rootScope.handleHttpError(data, status, headers, config);
        });

        $http({
          url : root_mapping + 'relation/relations',
          dataType : 'json',
          method : 'GET',
          headers : {
            'Content-Type' : 'application/json'
          }
        }).success(function(data) {
          $scope.mapRelations = data.mapRelation;
          localStorageService.add('mapRelations', data.mapRelation);
          $rootScope.$broadcast('localStorageModule.notification.setMapRelations', {
            key : 'mapRelations',
            mapRelations : data.mapRelations
          });
          $scope.allowableMapRelations = localStorageService.get('mapRelations');
        }).error(function(data, status, headers, config) {
          $rootScope.handleHttpError(data, status, headers, config);
        });

        $http({
          url : root_mapping + 'principle/principles',
          dataType : 'json',
          method : 'GET',
          headers : {
            'Content-Type' : 'application/json'
          }
        }).success(function(data) {
          $scope.mapPrinciples = data.mapPrinciple;
          localStorageService.add('mapPrinciples', data.mapPrinciple);
          $rootScope.$broadcast('localStorageModule.notification.setMapPrinciples', {
            key : 'mapPrinciples',
            mapPrinciples : data.mapPrinciples
          });
          $scope.allowableMapPrinciples = localStorageService.get('mapPrinciples');
        }).error(function(data, status, headers, config) {
          $rootScope.handleHttpError(data, status, headers, config);
        });

        $http({
          url : root_mapping + 'ageRange/ageRanges',
          dataType : 'json',
          method : 'GET',
          headers : {
            'Content-Type' : 'application/json'
          }
        }).success(function(data) {
          $scope.mapAgeRanges = data.mapAgeRange;
          localStorageService.add('mapAgeRanges', data.mapAgeRange);
          $rootScope.$broadcast('localStorageModule.notification.setMapAgeRanges', {
            key : 'mapAgeRanges',
            mapAgeRanges : data.mapAgeRanges
          });
          $scope.allowableMapAgeRanges = localStorageService.get('mapAgeRanges');
        }).error(function(data, status, headers, config) {
          $rootScope.handleHttpError(data, status, headers, config);
        });

        $http({
          url : root_reporting + 'definition/definitions',
          dataType : 'json',
          method : 'GET',
          headers : {
            'Content-Type' : 'application/json'
          }
        }).success(function(data) {
          // $scope.reportDefinitions
          // = data.reportDefinition;
          for (var i = 0; i < data.reportDefinition.length; i++) {
            $scope.reportDefinitions.push(data.reportDefinition[i]);
          }
          localStorageService.add('reportDefinitions', $scope.reportDefinitions);
          $rootScope.$broadcast('localStorageModule.notification.setMapRelations', {
            key : 'reportDefinitions',
            reportDefinitions : $scope.reportDefinitions
          });
          $scope.allowableReportDefinitions = localStorageService.get('reportDefinitions');
        }).error(function(data, status, headers, config) {
          $rootScope.handleHttpError(data, status, headers, config);
        });

        $http({
          url : root_reporting + 'qaCheckDefinition/qaCheckDefinitions',
          dataType : 'json',
          method : 'GET',
          headers : {
            'Content-Type' : 'application/json'
          }
        }).success(function(data) {
          for (var i = 0; i < data.reportDefinition.length; i++) {
            $scope.qaCheckDefinitions.push(data.reportDefinition[i]);
          }
          localStorageService.add('qaCheckDefinitions', data.reportDefinition);
          $rootScope.$broadcast('localStorageModule.notification.setQACheckDefinitions', {
            key : 'qaCheckDefinitions',
            qaCheckDefinitions : $scope.qaCheckDefinitions
          });
          $scope.allowableQACheckDefinitions = localStorageService.get('qaCheckDefinitions');
        }).error(function(data, status, headers, config) {
          $rootScope.handleHttpError(data, status, headers, config);
        });

        // find selected elements from the allowable
        // lists
        $scope.selectedMapType = $scope.getSelectedMapType();
        $scope.selectedMapRelationStyle = $scope.getSelectedMapRelationStyle();
        $scope.selectedWorkflowType = $scope.getSelectedWorkflowType();

        /*
                 * // determine if this project has a principles document if
                 * ($scope.focusProject.destinationTerminology == 'ICD10') {
                 * $scope.focusProject.mapPrincipleDocumentPath = 'doc/';
                 * $scope.focusProject.mapPrincipleDocument = 'ICD10_MappingPersonnelHandbook.docx';
                 * $scope.focusProject.mapPrincipleDocumentName = 'Mapping Personnel Handbook'; }
                 * else { $scope.focusProject.mapPrincipleDocument = null; }
                 */

        // set the scope maps
        $scope.scopeMap = {};
        $scope.scopeExcludedMap = {};

        // set pagination variables
        $scope.pageSize = 5;
        $scope.maxSize = 5;
        $scope.getPagedAdvices(1);
        $scope.getPagedRelations(1);
        $scope.getPagedPrinciples(1);
        $scope.getPagedScopeConcepts(1);
        $scope.getPagedScopeExcludedConcepts(1);
        $scope.getPagedReportDefinitions(1);

        // need to initialize selected qa check definitions since they
        // are persisted in the
        // report definition array
        $scope.focusProject.qaCheckDefinition = new Array();
        for (var i = 0; i < $scope.focusProject.reportDefinition.length; i++) {
          if ($scope.focusProject.reportDefinition[i].qacheck == true)
            $scope.focusProject.qaCheckDefinition.push($scope.focusProject.reportDefinition[i]);
        }
        $scope.getPagedQACheckDefinitions(1);
        $scope.orderProp = 'id';
      };

      $scope.goMapRecords = function() {
        // redirect page
        var path = '/project/records';
        $location.path(path);
      };

      // function to return trusted html code (for tooltip
      // content)
      $scope.to_trusted = function(html_code) {
        return $sce.trustAsHtml(html_code);
      };

      // /////////////////////////////////////////////////////////////
      // Functions to display and filter advices and
      // principles
      // NOTE: This is a workaround due to pagination
      // issues
      // /////////////////////////////////////////////////////////////

      // get paged functions
      // - sorts (by id) filtered elements
      // - counts number of filtered elmeents
      // - returns artificial page via slice

      $scope.getPagedAdvices = function(page, filter) {
        $scope.adviceFilter = filter;
        $scope.pagedAdvice = $scope.sortByKey($scope.focusProject.mapAdvice, 'name').filter(
          containsAdviceFilter);
        $scope.pagedAdviceCount = $scope.pagedAdvice.length;
        $scope.pagedAdvice = $scope.pagedAdvice.slice((page - 1) * $scope.pageSize, page
          * $scope.pageSize);
      };

      $scope.getPagedRelations = function(page, filter) {
        $scope.relationFilter = filter;
        $scope.pagedRelation = $scope.sortByKey($scope.focusProject.mapRelation, 'name').filter(
          containsRelationFilter);
        $scope.pagedRelationCount = $scope.pagedRelation.length;
        $scope.pagedRelation = $scope.pagedRelation.slice((page - 1) * $scope.pageSize, page
          * $scope.pageSize);
      };

      $scope.getPagedPrinciples = function(page, filter) {
        $scope.principleFilter = filter;
        $scope.pagedPrinciple = $scope.sortByKey($scope.focusProject.mapPrinciple, 'principleId')
          .filter(containsPrincipleFilter);
        $scope.pagedPrincipleCount = $scope.pagedPrinciple.length;
        $scope.pagedPrinciple = $scope.pagedPrinciple.slice((page - 1) * $scope.pageSize, page
          * $scope.pageSize);

      };

      $scope.getPagedReportDefinitions = function(page, filter) {
        $scope.reportDefinitionFilter = filter;
        $scope.pagedReportDefinition = $scope.sortByKey($scope.focusProject.reportDefinition,
          'name').filter(containsReportDefinitionFilter);
        // remove qa check report definitions from the list; they have
        // their own section
        for (var j = 0; j < $scope.pagedReportDefinition.length; j++) {
          if ($scope.pagedReportDefinition[j].qacheck == true) {
            $scope.pagedReportDefinition.splice(j, 1);
          }
        }
        $scope.pagedReportDefinitionCount = $scope.pagedReportDefinition.length;
        $scope.pagedReportDefinition = $scope.pagedReportDefinition.slice((page - 1)
          * $scope.pageSize, page * $scope.pageSize);

      };

      $scope.getPagedQACheckDefinitions = function(page, filter) {
        $scope.qaCheckDefinitionFilter = filter;
        $scope.pagedQACheckDefinition = $scope.sortByKey($scope.focusProject.qaCheckDefinition,
          'name').filter(containsQACheckDefinitionFilter);
        $scope.pagedQACheckDefinitionCount = $scope.pagedQACheckDefinition.length;
        $scope.pagedQACheckDefinition = $scope.pagedQACheckDefinition.slice((page - 1)
          * $scope.pageSize, page * $scope.pageSize);

      };

      $scope.getPagedScopeConcepts = function(page) {
        console.debug('Called paged scope concept for page ', page);

        // construct a paging/filtering/sorting object
        var pfsParameterObj = {
          'startIndex' : page == -1 ? -1 : (page - 1) * $scope.pageSize,
          'maxResults' : page == -1 ? -1 : $scope.pageSize,
          'sortField' : '',
          'queryRestriction' : ''
        };

        $rootScope.glassPane++;

        $http({
          url : root_mapping + 'project/id/' + $scope.focusProject.id + '/scopeConcepts',
          dataType : 'json',
          data : pfsParameterObj,
          method : 'POST',
          headers : {
            'Content-Type' : 'application/json'
          }
        }).success(function(data) {
          console.debug('  scope concepts = ', data);
          $rootScope.glassPane--;
          $scope.pagedScopeConcept = data.searchResult;
          $scope.pagedScopeConceptCount = data.totalCount;
        }).error(function(data, status, headers, config) {
          $rootScope.glassPane--;

          $rootScope.handleHttpError(data, status, headers, config);
        });
      };

      $scope.getPagedScopeExcludedConcepts = function(page, filter) {
        console.debug('Called paged scope concept for page ', page);
        // construct a paging/filtering/sorting object
        var pfsParameterObj = {
          'startIndex' : page == -1 ? -1 : (page - 1) * $scope.pageSize,
          'maxResults' : page == -1 ? -1 : $scope.pageSize,
          'sortField' : '',
          'queryRestriction' : ''
        };

        $rootScope.glassPane++;
        $http({
          url : root_mapping + 'project/id/' + $scope.focusProject.id + '/scopeExcludedConcepts',
          dataType : 'json',
          data : pfsParameterObj,
          method : 'POST',
          headers : {
            'Content-Type' : 'application/json'
          }
        }).success(function(data) {
          console.debug('  scope excluded = ', data);
          $rootScope.glassPane--;
          $scope.pagedScopeExcludedConcept = data.searchResult;
          $scope.pagedScopeExcludedConceptCount = data.totalCount;
        }).error(function(data, status, headers, config) {
          $rootScope.glassPane--;
          $rootScope.handleHttpError(data, status, headers, config);
        });
      };

      // functions to reset the filter and retrieve
      // unfiltered results

      $scope.resetAdviceFilter = function() {
        $scope.adviceFilter = '';
        $scope.getPagedAdvices(1);
      };

      $scope.resetRelationFilter = function() {
        $scope.relationFilter = '';
        $scope.getPagedRelations(1);
      };

      $scope.resetPrincipleFilter = function() {
        $scope.principleFilter = '';
        $scope.getPagedPrinciples(1);
      };

      $scope.resetScopeConceptFilter = function() {
        $scope.scopeConceptFilter = '';
        $scope.getPagedScopeConcepts(1);
      };

      $scope.resetReportDefinitionFilter = function() {
        $scope.reportDefinitionFilter = '';
        $scope.getPagedReportDefinitions(1);
      };

      $scope.resetQACheckDefinitionFilter = function() {
        $scope.qaCheckDefinitionFilter = '';
        $scope.getPagedQACheckDefinitions(1);
      };

      $scope.resetScopeExcludedConceptFilter = function() {
        $scope.scopeExcludedConceptFilter = '';
        $scope.getPagedScopeExcludedConcepts(1);
      };

      // element-specific functions for filtering
      // do not want to search id or objectId

      function containsAdviceFilter(element) {
        // check if advice filter is empty
        if ($scope.adviceFilter === '' || $scope.adviceFilter == null)
          return true;

        // otherwise check if upper-case advice filter
        // matches upper-case element name or detail
        if (element.detail.toString().toUpperCase().indexOf(
          $scope.adviceFilter.toString().toUpperCase()) != -1)
          return true;
        if (element.name.toString().toUpperCase().indexOf(
          $scope.adviceFilter.toString().toUpperCase()) != -1)
          return true;

        // otherwise return false
        return false;
      }

      function containsRelationFilter(element) {
        // check if relation filter is empty
        if ($scope.relationFilter === '' || $scope.relationFilter == null)
          return true;

        // otherwise check if upper-case relation filter
        // matches upper-case element name or detail
        if (element.terminologyId.toString().toUpperCase().indexOf(
          $scope.relationFilter.toString().toUpperCase()) != -1)
          return true;
        if (element.name.toString().toUpperCase().indexOf(
          $scope.relationFilter.toString().toUpperCase()) != -1)
          return true;

        // otherwise return false
        return false;
      }

      function containsPrincipleFilter(element) {

        // check if principle filter is empty
        if ($scope.principleFilter === '' || $scope.principleFilter == null)
          return true;

        // otherwise check if upper-case principle
        // filter matches upper-case element name or
        // detail
        if (element.principleId.toString().toUpperCase().indexOf(
          $scope.principleFilter.toString().toUpperCase()) != -1)
          return true;
        // if (
        // element.detail.toString().toUpperCase().indexOf(
        // $scope.principleFilter.toString().toUpperCase())
        // != -1) return true;
        if (element.name.toString().toUpperCase().indexOf(
          $scope.principleFilter.toString().toUpperCase()) != -1)
          return true;
        if (element.sectionRef != null
          && element.sectionRef.toString().toUpperCase().indexOf(
            $scope.principleFilter.toString().toUpperCase()) != -1)
          return true;

        // otherwise return false
        return false;
      }

      function containsScopeConceptFilter(element) {

        // check if scopeConcept filter is empty
        if ($scope.scopeConceptFilter === '' || $scope.scopeConceptFilter == null)
          return true;

        // otherwise check if upper-case scopeConcept
        // filter matches upper-case element name or
        // detail
        if (element.scopeConceptId.toString().toUpperCase().indexOf(
          $scope.scopeConceptFilter.toString().toUpperCase()) != -1)
          return true;
        if (element.name.toString().toUpperCase().indexOf(
          $scope.scopeConceptFilter.toString().toUpperCase()) != -1)
          return true;

        // otherwise return false
        return false;
      }

      function containsScopeExcludedConceptFilter(element) {

        // check if scopeConcept filter is empty
        if ($scope.scopeExcludesConceptFilter === '' || $scope.scopeExcludesConceptFilter == null)
          return true;

        // otherwise check if upper-case scopeConcept
        // filter matches upper-case element name or
        // detail
        if (element.scopeExcludesConceptId.toString().toUpperCase().indexOf(
          $scope.scopeExcludesConceptFilter.toString().toUpperCase()) != -1)
          return true;
        if (element.name.toString().toUpperCase().indexOf(
          $scope.scopeExcludesConceptFilter.toString().toUpperCase()) != -1)
          return true;

        // otherwise return false
        return false;
      }

      function containsReportDefinitionFilter(element) {
        // check if reportDefinition filter is empty
        if ($scope.reportDefinitionFilter === '' || $scope.reportDefinitionFilter == null)
          return true;

        // otherwise check if upper-case
        // reportDefinition filter
        // matches upper-case element name or detail
        if (element.name.toString().toUpperCase().indexOf(
          $scope.reportDefinitionFilter.toString().toUpperCase()) != -1)
          return true;

        // otherwise return false
        return false;
      }

      function containsQACheckDefinitionFilter(element) {

        // check if qaCheckDefinition filter is empty
        if ($scope.qaCheckDefinitionFilter === '' || $scope.qaCheckDefinitionFilter == null)
          return true;

        // otherwise check if upper-case
        // qaCheckDefinition filter
        // matches upper-case element name or detail
        if (element.name.toString().toUpperCase().indexOf(
          $scope.qaCheckDefinitionFilter.toString().toUpperCase()) != -1)
          return true;

        // otherwise return false
        return false;
      }

      // helper function to sort a JSON array by field

      $scope.sortByKey = function sortById(array, key) {
        return array.sort(function(a, b) {
          var x, y;
          // if a number
          if (!isNaN(parseInt(a[key]))) {
            x = a[key];
            y = b[key];
          } else {
            x = new String(a[key]).toUpperCase();
            y = new String(b[key]).toUpperCase();
          }
          if (x < y)
            return -1;
          if (x > y)
            return 1;
          return 0;
        });
      };

      // function to change project from the header
      $scope.changeFocusProject = function(mapProject) {
        $scope.focusProject = mapProject;

        // update and broadcast the new focus project
        localStorageService.add('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });

        // update the user preferences
        $scope.preferences.lastMapProjectId = $scope.focusProject.id;
        localStorageService.add('preferences', $scope.preferences);
        $rootScope.$broadcast('localStorageModule.notification.setUserPreferences', {
          key : 'userPreferences',
          userPreferences : $scope.preferences
        });

      };

      $scope.goToHelp = function() {
        var path;
        if ($scope.page != 'mainDashboard') {
          path = 'help/' + $scope.page + 'Help.html';
        } else {
          path = 'help/' + $scope.currentRole + 'DashboardHelp.html';
        }
        // redirect page
        $location.path(path);
      };

      $scope.isEmailViewable = function(email) {
        if (email.indexOf('ihtsdo.org') > -1) {
          return true;
        } else {
          return false;
        }
      };

      $scope.toggleEditMode = function() {
        if ($scope.editModeEnabled == true) {
          $scope.editModeEnabled = false;
          $scope.updateMapProject();
        } else {
          $scope.editModeEnabled = true;
        }
      };

      $scope.getSelectedMapRelationStyle = function() {
        for (var j = 0; j < $scope.allowableMapRelationStyles.length; j++) {
          if ($scope.focusProject.mapRelationStyle === $scope.allowableMapRelationStyles[j].name)
            return $scope.allowableMapRelationStyles[j];
        }
        return null;
      };

      $scope.selectMapRelationStyle = function() {
        // update and broadcast the updated focus
        // project
        $scope.focusProject.mapRelationStyle = $scope.selectedMapRelationStyle.name;
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
        $scope.updateMapProject();
      };

      $scope.getSelectedMapType = function() {
        for (var j = 0; j < $scope.allowableMapTypes.length; j++) {
          if ($scope.focusProject.mapRefsetPattern === $scope.allowableMapTypes[j].name)
            return $scope.allowableMapTypes[j];
        }
        return null;
      };

      $scope.selectMapType = function() {
        $scope.focusProject.mapType = $scope.selectedMapType.name;
        // update and broadcast the updated focus
        // project
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
        $scope.updateMapProject();
      };

      $scope.getSelectedWorkflowType = function() {
        for (var j = 0; j < $scope.allowableWorkflowTypes.length; j++) {
          if ($scope.focusProject.workflowType === $scope.allowableWorkflowTypes[j].name)
            return $scope.allowableWorkflowTypes[j];
        }
        return null;

      };

      $scope.selectWorkflowType = function() {
        $scope.focusProject.workflowType = $scope.selectedWorkflowType.name;
        // update and broadcast the updated focus
        // project
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
        $scope.updateMapProject();
      };

      $scope.deleteLead = function(lead) {
        for (var j = 0; j < $scope.focusProject.mapLead.length; j++) {
          if (lead.userName === $scope.focusProject.mapLead[j].userName) {
            $scope.focusProject.mapLead.splice(j, 1);
          }
        }
        // update and broadcast the updated focus
        // project
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
      };

      $scope.addLead = function(user) {
        for (var i = 0; i < $scope.focusProject.mapSpecialist.length; i++) {
          if ($scope.focusProject.mapSpecialist[i].name == user.name) {
            confirm('User ' + user.name
              + ' is already a Map Specialist.\nUser cannot have more than one role.');
            return;
          }
        }
        $scope.focusProject.mapLead.push(user);
        // update and broadcast the updated focus
        // project
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
      };

      $scope.addMapUserToMapProjectWithRole = function(user, role) {

        // check role
        if (role != 'Specialist' && role != 'Lead') {
          return;
        }

        // check user non-null
        if (user == null || user == undefined) {
          return;
        }

        // check user valid
        if (user.userName == null || user.userName == undefined) {
          alert('You must specify a login name.');
          return;
        }

        if (user.name == null || user.name == undefined) {
          alert('You must specify the user\'s name');
          return;
        }

        if (user.email == null || user.email == undefined) {
          alert('You must specify the user\'s email.  Enter "none" or similar text if unknown');
          return;
        }

        // by default the application role is Viewer
        user.applicationRole = 'VIEWER';

        // add the user
        $http({
          url : root_mapping + 'user/add',
          dataType : 'json',
          data : user,
          method : 'PUT',
          headers : {
            'Content-Type' : 'application/json'
          }
        }).success(function(data) {
          // copy the newly updated object with id
          var user = data;

          $scope.mapUsers.push(user);
          localStorageService.add('mapUsers', $scope.mapUsers);

          // add this user to the focus project
          if (role == 'Specialist')
            $scope.focusProject.mapSpecialist.push(user);
          else if (role == 'Lead')
            $scope.focusProject.mapLead.push(user);

          // update the project
          $scope.updateMapProject();

        }).error(function(data, status, headers, config) {
          $rootScope.handleHttpError(data, status, headers, config);
        });

      };

      $scope.deleteSpecialist = function(specialist) {
        for (var j = 0; j < $scope.focusProject.mapSpecialist.length; j++) {
          if (specialist.userName === $scope.focusProject.mapSpecialist[j].userName) {
            $scope.focusProject.mapSpecialist.splice(j, 1);
          }
        }
        // update and broadcast the updated focus
        // project
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
      };

      $scope.addSpecialist = function(user) {
        for (var i = 0; i < $scope.focusProject.mapLead.length; i++) {
          if ($scope.focusProject.mapLead[i].name == user.name) {
            confirm('User ' + user.name
              + ' is already a Map Lead.\nUser cannot have more than one role.');
            return;
          }
        }

        $scope.focusProject.mapSpecialist.push(user);
        // update and broadcast the updated focus
        // project
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
      };

      $scope.deleteAdvice = function(advice) {
        for (var j = 0; j < $scope.focusProject.mapAdvice.length; j++) {
          if (advice.name === $scope.focusProject.mapAdvice[j].name) {
            $scope.focusProject.mapAdvice.splice(j, 1);
          }
        }
        // update and broadcast the updated focus
        // project
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
        $scope.pageAdvice = 1;
        $scope.resetAdviceFilter();
        $scope.updateMapProject();
      };

      $scope.addAdvice = function(advice) {
        $scope.focusProject.mapAdvice.push(advice);
        // update and broadcast the updated focus
        // project
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
        $scope.resetAdviceFilter();
        $scope.updateMapProject();
      };

      $scope.updateAdvice = function(advice) {
        console.debug('updateAdvice');
        $http({
          url : root_mapping + 'advice/update',
          dataType : 'json',
          data : advice,
          method : 'POST',
          headers : {
            'Content-Type' : 'application/json'
          }
        }).success(function(data) {
          console.debug('  success', data);
        }).error(function(data, status, headers, config) {
          $scope.recordError = 'Error updating map advice.';
          $rootScope.handleHttpError(data, status, headers, config);
        }).then(function(data) {
          $http({
            url : root_mapping + 'advice/advices',
            dataType : 'json',
            method : 'GET',
            headers : {
              'Content-Type' : 'application/json'
            }
          }).success(function(data) {
            $scope.mapAdvices = data.mapAdvice;
            for (var j = 0; j < $scope.focusProject.mapAdvice.length; j++) {
              if (advice.id === $scope.focusProject.mapAdvice[j].id) {
                $scope.focusProject.mapAdvice[j] = advice;
              }
            }
            localStorageService.add('mapAdvices', data.mapAdvice);
            $rootScope.$broadcast('localStorageModule.notification.setMapAdvices', {
              key : 'mapAdvices',
              mapAdvices : data.mapAdvices
            });
            $scope.allowableMapAdvices = localStorageService.get('mapAdvices');

            // update
            // and
            // broadcast
            // the
            // updated
            // focus
            // project
            localStorageService.add('focusProject', $scope.focusProject);
            $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
              key : 'focusProject',
              focusProject : $scope.focusProject
            });

            $scope.updateMapProject();

          }).error(function(data, status, headers, config) {
            $rootScope.handleHttpError(data, status, headers, config);
          });

        });
      };

      $scope.submitNewMapAdvice = function(mapAdviceName, mapAdviceDetail, allowableForNullTarget,
        isComputed) {
        console.debug('submitNewMapAdvice');
        var obj = {
          'name' : mapAdviceName,
          'detail' : mapAdviceDetail,
          'isAllowableForNullTarget' : allowableForNullTarget,
          'isComputed' : isComputed
        };

        $rootScope.glassPane++;
        $http({
          url : root_mapping + 'advice/add',
          dataType : 'json',
          data : obj,
          method : 'PUT',
          headers : {
            'Content-Type' : 'application/json'
          }
        }).success(function(data) {
          $rootScope.glassPane--;
          console.debug('  success', data);

          // add the new advice to the available list
          $scope.mapAdvices.push(data);
          $scope.allowableMapAdvices.push(data);

          // add the new advice to the current project
          $scope.focusProject.mapAdvice.push(data);

          // update the map project
          $scope.updateMapProject().then(function(response) {
            $scope.resetAdviceFilter();
          });

        }).error(function(data, status, headers, config) {
          $rootScope.glassPane--;
          $rootScope.handleHttpError(data, status, headers, config);
        });

      };

      $scope.deleteRelation = function(relation) {
        for (var j = 0; j < $scope.focusProject.mapRelation.length; j++) {
          if (relation.name === $scope.focusProject.mapRelation[j].name) {
            $scope.focusProject.mapRelation.splice(j, 1);
          }
        }
        // update and broadcast the updated focus
        // project
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
        $scope.resetRelationFilter();
        $scope.updateMapProject();
      };

      $scope.addRelation = function(relation) {
        $scope.focusProject.mapRelation.push(relation);
        // update and broadcast the updated focus
        // project
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
        $scope.resetRelationFilter();
        $scope.updateMapProject();
      };

      $scope.submitNewMapRelation = function(relation) {
        console.debug('submitNewMapRelation for application');

        $rootScope.glassPane++;
        $http({
          url : root_mapping + 'relation/add',
          dataType : 'json',
          data : relation,
          method : 'PUT',
          headers : {
            'Content-Type' : 'application/json'
          }
        })

        .success(function(data) {
          $rootScope.glassPane--;
          console.debug('  success', data);
          // add new relations to the sets
          $scope.mapRelations.push(data);
          $scope.allowableMapRelations.push(data);

          // add the new advice to the current project
          $scope.focusProject.mapRelation.push(data);

          // update the map project
          $scope.updateMapProject().then(function() {
            $scope.resetRelationFilter();
          });

        }).error(function(data, status, headers, config) {
          $rootScope.glassPane--;
          $rootScope.handleHttpError(data, status, headers, config);
        });
      };

      $scope.deleteReportDefinition = function(reportDefinition) {
        for (var j = 0; j < $scope.focusProject.reportDefinition.length; j++) {
          if (reportDefinition.name === $scope.focusProject.reportDefinition[j].name) {
            $scope.focusProject.reportDefinition.splice(j, 1);
          }
        }
        // update and broadcast the updated focus
        // project
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
        $scope.resetReportDefinitionFilter();
        $scope.updateMapProject();
      };

      $scope.deleteQACheckDefinition = function(qaCheckDefinition) {
        // check the local qaCheck set
        for (var j = 0; j < $scope.focusProject.qaCheckDefinition.length; j++) {
          if (qaCheckDefinition.id === $scope.focusProject.qaCheckDefinition[j].id) {
            $scope.focusProject.qaCheckDefinition.splice(j, 1);
          }
        }

        // also need to remove from the project, qa definitions are in
        // reportDefinitions collection
        for (var j = 0; j < $scope.focusProject.reportDefinition.length; j++) {
          if (qaCheckDefinition.id === $scope.focusProject.reportDefinition[j].id) {
            $scope.focusProject.reportDefinition.splice(j, 1);
          }
        }
        // update and broadcast the updated focus
        // project
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
        $scope.resetQACheckDefinitionFilter();
        $scope.updateMapProject();
      };

      $scope.addReportDefinition = function(reportDefinition) {
        $scope.focusProject.reportDefinition.push(reportDefinition);
        console.debug($scope.focusProject.reportDefinition);
        // update and broadcast the updated focus
        // project
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
        $scope.resetReportDefinitionFilter();
        $scope.updateMapProject();
      };

      $scope.addQACheckDefinition = function(qaCheckDefinition) {
        $scope.focusProject.qaCheckDefinition.push(qaCheckDefinition);
        $scope.focusProject.reportDefinition.push(qaCheckDefinition);
        // update and broadcast the updated focus
        // project
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
        $scope.resetQACheckDefinitionFilter();
        $scope.updateMapProject();
      };

      $scope.deletePrinciple = function(principle) {
        for (var j = 0; j < $scope.focusProject.mapPrinciple.length; j++) {
          if (principle.name === $scope.focusProject.mapPrinciple[j].name) {
            $scope.focusProject.mapPrinciple.splice(j, 1);
          }
        }
        // update and broadcast the updated focus
        // project
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
        $scope.resetPrincipleFilter();
        $scope.updateMapProject();
      };

      $scope.addPrinciple = function(principle) {
        $scope.focusProject.mapPrinciple.push(principle);
        // update and broadcast the updated focus
        // project
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
        $scope.resetPrincipleFilter();
        $scope.updateMapProject();
      };

      $scope.updatePrinciple = function(principle) {
        console.debug(' updatePrinciple', principle);
        $http({
          url : root_mapping + 'principle/update',
          dataType : 'json',
          data : principle,
          method : 'POST',
          headers : {
            'Content-Type' : 'application/json'
          }
        }).success(function(data) {
          console.debug('  success', data);
        }).error(function(data, status, headers, config) {
          $scope.recordError = 'Error updating map principle.';
          $rootScope.handleHttpError(data, status, headers, config);
        }).then(function(data) {
          $http({
            url : root_mapping + 'principle/principles',
            dataType : 'json',
            method : 'GET',
            headers : {
              'Content-Type' : 'application/json'
            }
          }).success(function(data) {

            $scope.mapPrinciples = data.mapPrinciple;
            for (var j = 0; j < $scope.focusProject.mapPrinciple.length; j++) {
              if (principle.id === $scope.focusProject.mapPrinciple[j].id) {
                $scope.focusProject.mapPrinciple[j] = principle;
              }
            }
            localStorageService.add('mapPrinciples', data.mapPrinciple);
            $rootScope.$broadcast('localStorageModule.notification.setMapPrinciples', {
              key : 'mapPrinciples',
              mapPrinciples : data.mapPrinciples
            });
            $scope.allowableMapPrinciples = localStorageService.get('mapPrinciples');

            // update
            // and
            // broadcast
            // the
            // updated
            // focus
            // project
            localStorageService.add('focusProject', $scope.focusProject);
            $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
              key : 'focusProject',
              focusProject : $scope.focusProject
            });

            $scope.updateMapProject();
          }).error(function(data, status, headers, config) {
            $rootScope.handleHttpError(data, status, headers, config);
          });

        });
      };

      $scope.submitNewMapPrinciple = function(principle) {
        console.debug('submitNewMapPrinciple', principle);

        $rootScope.glassPane++;
        $http({
          url : root_mapping + 'principle/add',
          dataType : 'json',
          data : principle,
          method : 'PUT',
          headers : {
            'Content-Type' : 'application/json'
          }
        }).success(function(data) {
          $rootScope.glassPane--;
          console.debug('  success', data);

          // add principle to the local sets
          $scope.mapPrinciples.push(data);
          $scope.allowableMapPrinciples.push(data);

          // add the new advice to the current project
          $scope.focusProject.mapPrinciple.push(data);

          // update the map project
          $scope.updateMapProject().then(function() {
            $scope.resetPrincipleFilter();
          });

        }).error(function(data, status, headers, config) {
          $rootScope.glassPane--;
          $rootScope.handleHttpError(data, status, headers, config);
        });
      };

      $scope.deleteAgeRange = function(ageRange) {
        for (var j = 0; j < $scope.focusProject.mapAgeRange.length; j++) {
          if (ageRange.name === $scope.focusProject.mapAgeRange[j].name) {
            $scope.focusProject.mapAgeRange.splice(j, 1);
          }
        }
        // update and broadcast the updated focus
        // project
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
        $scope.updateMapProject();
      };

      $scope.addAgeRange = function(ageRange) {
        $scope.focusProject.mapAgeRange.push(ageRange);
        // update and broadcast the updated focus
        // project
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
        $scope.updateMapProject();
      };

      $scope.submitNewMapAgeRange = function(ageRange) {
        console.debug('submitNewMapAgeRange', ageRange);
        $rootScope.glassPane++;

        $http({
          url : root_mapping + 'ageRange/add',
          dataType : 'json',
          data : ageRange,
          method : 'PUT',
          headers : {
            'Content-Type' : 'application/json'
          }
        }).success(function(data) {
          $rootScope.glassPane--;
          console.debug('  success', data);

          // add principle to the local sets
          $scope.mapAgeRanges.push(data);
          $scope.allowableMapAgeRanges.push(data);

          // add the new advice to the current project
          $scope.focusProject.mapAgeRange.push(data);

          // update the map project
          $scope.updateMapProject().then(function() {
            $scope.resetAgeRangeFilter();
          });

        }).error(function(data, status, headers, config) {
          $rootScope.glassPane--;
          $rootScope.handleHttpError(data, status, headers, config);
        });
      };

      $scope.submitNewErrorMessage = function(message) {
        var localErrorMessages = $scope.focusProject.errorMessages;
        localErrorMessages.push(message);
        $scope.focusProject.errorMessages = localErrorMessages;
        $scope.updateMapProject();
      };

      $scope.deleteErrorMessage = function(message) {
        for (var j = 0; j < $scope.focusProject.errorMessages.length; j++) {
          if (message === $scope.focusProject.errorMessages[j]) {
            $scope.focusProject.errorMessages.splice(j, 1);
          }
        }
        // update and broadcast the updated focus
        // project
        localStorageService.set('focusProject', $scope.focusProject);
        $rootScope.$broadcast('localStorageModule.notification.setFocusProject', {
          key : 'focusProject',
          focusProject : $scope.focusProject
        });
        $scope.updateMapProject();
      };

      // ////////////////////////////////////////////
      // Scope Include Concept Addition/Removal
      // ////////////////////////////////////////////

      // remove a single concept (using the [x] button)
      $scope.removeScopeIncludedConcept = function(scopeConcept, currentPage) {
        console.debug('removeScopeIncludedConcept', scopeConcept, currentPage);
        $rootScope.glassPane++;

        $http({
          url : root_mapping + 'project/id/' + $scope.focusProject.id + '/scopeConcept/remove',
          data : scopeConcept.terminologyId,
          method : 'POST',
          headers : {
            'Content-Type' : 'text/plain'
          }
        }).success(function(data) {
          console.debug('  success', data);
          $rootScope.glassPane--;

          // re-page the scope concepts
          $scope.getPagedScopeConcepts(currentPage);
        }).error(function(data, status, headers, config) {

          $rootScope.glassPane--;
          $rootScope.handleHttpError(data, status, headers, config);
        });

      };

      // remove a single/batch of excluded concepts
      $scope.removeScopeIncludedConcepts = function(scopeConceptsUnsplit) {
        console.debug('removeScopeIncludedConcepts', scopeConceptsUnsplit);
        $rootScope.glassPane++;
        var scopeConcepts = scopeConceptsUnsplit.split(/,\s*|\s+/);
        $http({
          url : root_mapping + 'project/id/' + $scope.focusProject.id + '/scopeConcepts/remove',
          dataType : 'json',
          data : scopeConcepts,
          method : 'POST',
          headers : {
            'Content-Type' : 'application/json'
          }
        }).success(function(data) {
          console.debug('  success', data);
          $rootScope.glassPane--;
          $scope.getPagedScopeConcepts(1);

        }).error(function(data, status, headers, config) {
          $rootScope.glassPane--;

          $rootScope.handleHttpError(data, status, headers, config);
        });
      };

      // submit a single/batch of concepts for addition
      $scope.submitNewScopeIncludedConcepts = function(scopeConceptsUnsplit) {
        console.debug('submitNewScopeIncludedConcept', scopeConceptsUnsplit);

        $rootScope.glassPane++;

        var scopeConcepts = scopeConceptsUnsplit.split(/,\s*|\s+/);

        $http({
          url : root_mapping + 'project/id/' + $scope.focusProject.id + '/scopeConcepts/add',
          dataType : 'json',
          data : scopeConcepts,
          method : 'POST',
          headers : {
            'Content-Type' : 'application/json'
          }
        }).success(function(data) {
          $rootScope.glassPane--;
          console.debug('  success', data);
          $scope.scopeWarnings = data.warnings;
          $scope.resetScopeConceptFilter();

        }).error(function(data, status, headers, config) {
          $rootScope.handleHttpError(data, status, headers, config);
        });

      };

      // ////////////////////////////////////////////
      // Scope Exclude Concept Addition/Removal
      // ////////////////////////////////////////////

      // remove a single concept (using the [x] button)
      $scope.removeScopeExcludedConcept = function(scopeExcludedConcept, currentPage) {
        console.debug('removeScopeExcludedConcept', scopeExcludedConcept, currentPage);
        $rootScope.glassPane++;

        $http(
          {
            url : root_mapping + 'project/id/' + $scope.focusProject.id
              + '/scopeExcludedConcept/remove',
            dataType : 'json',
            data : scopeExcludedConcept.terminologyId,
            method : 'POST',
            headers : {
              'Content-Type' : 'application/json'
            }
          }).success(function(data) {
          console.debug('  success', data);
          $rootScope.glassPane--;

          $scope.getPagedScopeExcludedConcepts(currentPage);

        }).error(function(data, status, headers, config) {
          $rootScope.handleHttpError(data, status, headers, config);
        });
      };

      // remove a single/batch of excluded concepts
      $scope.removeScopeExcludedConcepts = function(scopeExcludedConceptsUnsplit) {
        console.debug('removeScopeExcludedConcepts', scopeExcludedConceptsUnsplit);
        $rootScope.glassPane++;

        var scopeExcludedConcepts = scopeExcludedConceptsUnsplit.split(/,\s*|\s+/);
        $http(
          {
            url : root_mapping + 'project/id/' + $scope.focusProject.id
              + '/scopeExcludedConcepts/remove',
            dataType : 'json',
            data : scopeExcludedConcepts,
            method : 'POST',
            headers : {
              'Content-Type' : 'application/json'
            }
          }).success(function() {
          $rootScope.glassPane--;
          console.debug('  success', data);
          $scope.getPagedScopeExcludedConcepts(1);

        }).error(function(data, status, headers, config) {
          $rootScope.glassPane--;

          $rootScope.handleHttpError(data, status, headers, config);
        });
      };

      // submit a single/batch of concepts for addition
      $scope.submitNewScopeExcludedConcepts = function(scopeExcludedConceptsUnsplit) {
        console.debug('submitNewScopeExcludedConcept', scopeExcludedConceptsUnsplit);

        $rootScope.glassPane++;
        var scopeExcludedConcepts = scopeExcludedConceptsUnsplit.split(/,\s*|\s+/);
        $http(
          {
            url : root_mapping + 'project/id/' + $scope.focusProject.id
              + '/scopeExcludedConcepts/add',
            dataType : 'json',
            data : scopeExcludedConcepts,
            method : 'POST',
            headers : {
              'Content-Type' : 'application/json'
            }
          }).success(function(data) {
          console.debug('  success', data);
          $rootScope.glassPane--;
          $scope.getPagedScopeExcludedConcepts(1);
        }).error(function(data, status, headers, config) {
          $rootScope.glassPane--;

          $rootScope.handleHttpError(data, status, headers, config);
        });

      };

      $scope.updateCachedMapProjects = function() {

        // first, update focus project
        localStorageService.add('focusProject', $scope.focusProject);

        var mapProjects = $scope.mapProjects;

        // replace the focus project in the list
        for (var i = 0; i < mapProjects.length; i++) {
          if (mapProjects[i].id == $scope.focusProject.id) {
            mapProjects[i] = $scope.focusProject;
          }
        }

        // set the map projects in the cache
        localStorageService.add('mapProjects', mapProjects);

      };

      // /////////////////////////////////////
      // Model reset, clears all filters
      // /////////////////////////////////////

      $scope.resetModel = function() {
        angular.copy($scope.focusProjectBeforeChanges, $scope.focusProject);

        $scope.resetAdviceFilter();
        $scope.resetRelationFilter();
        $scope.resetPrincipleFilter();
        $scope.resetScopeConceptFilter();
        $scope.resetScopeExcludedConceptFilter();
        $scope.resetReportDefinitionFilter();
      };

      /**
       * Function to update a map project via REST call and update the cached
       * projects
       */
      $scope.updateMapProject = function() {
        console.debug('Update map project');
        // first, add the modified project to the cache
        localStorageService.add('focusProject', $scope.focusProject);

        var deferred = $q.defer();

        $rootScope.glassPane++;

        $http({
          url : root_mapping + 'project/update',
          dataType : 'json',
          data : $scope.focusProject,
          method : 'POST',
          headers : {
            'Content-Type' : 'application/json'
          }
        }).success(function(data) {
          console.debug('  success', data);
          $rootScope.glassPane--;

          // update the cached project list
          for (var i = 0; i < $scope.mapProjects.length; i++) {
            if ($scope.mapProjects[i].id = data.id) {
              $scope.mapProjects[i] = data;
            }
          }
          localStorageService.add('mapProjects', $scope.mapProjects);

          deferred.resolve();

        }).error(function(data, status, headers, config) {
          $rootScope.glassPane--;
          $rootScope.handleHttpError(data, status, headers, config);
          deferred.reject();
        });

        return deferred.promise;
      };

      $scope.uploadFile = function(file) {

        $rootScope.glassPane++;
        Upload.upload({
          url : root_mapping + 'upload/' + $scope.focusProject.id,
          data : {
            file : file
          }
        }).then(
          // Success
          function(response) {
            $rootScope.glassPane--;
          },
          // error
          function(response) {
            $rootScope.handleHttpError(response.data, response.status, response.headers,
              response.config);
            $rootScope.glassPane--;
          },
          // event
          function(evt) {
            var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
            console.debug('progress: ' + progressPercentage + '% ' + evt.config.data.file.name);
          });

      };

      ///////////////////////////////////////
      // Release Handling
      // /////////////////////////////////////

      $scope.release = {

        effectiveTime : null,
        moduleId : null,
        inputFile : null,
        startDate : null,
      }

      $scope.beginRelease = function() {
        var beginTime = new Date();

        if (!$scope.release.effectiveTime) {
          window.alert('Must set effective time to begin release');
        }

        $rootScope.glassPane++;
        $http.post(
          root_mapping + 'project/id/' + $scope.focusProject.id + '/release/'
            + $scope.release.effectiveTime + '/begin').then(

          // Success
          function(response) {
            $rootScope.glassPane--;
          },
          function(response) {
            $rootScope.glassPane--;
            $rootScope.handleHttpError(response.data, response.status, response.headers,
              response.config);
          })
      };

      $scope.processRelease = function() {
        $rootScope.glassPane++;

        if (!$scope.release.effectiveTime || !$scope.release.moduleId) {
          window.alert('Must set effective time and module id to process release');
        }

        // @Path("/project/id/{id:[0-9][0-9]*}/release/{effectiveTime}/module/id/{moduleId}/process")
        $http.post(
          root_mapping + 'project/id/' + $scope.focusProject.id + '/release/'
            + $scope.release.effectiveTime + '/module/id/' + $scope.release.moduleId + '/process')
          .then(

            // Success
            function(response) {
              $rootScope.glassPane--;

            },
            function(response) {
              $rootScope.glassPane--;
              $rootScope.handleHttpError(response.data, response.status, response.headers,
                response.config);
            })
      }

      $scope.finishRelease = function(testMode) {
        $rootScope.glassPane++;

        if (!$scope.release.effectiveTime) {
          window.alert('Must set effective time to finish or preview release');
          $rootScope.glassPane--;
          return;
        }

        if (!testMode && !window.confirm("Are you absolutely sure? This action cannot be undone")) {
          $rootScope.glassPane--;
          return;
        }

        // @Path("/project/id/{id:[0-9][0-9]*}/release/{effectiveTime}/finish")
        $http.post(
          root_mapping + 'project/id/' + $scope.focusProject.id + '/release/'
            + $scope.release.effectiveTime + '/finish' + (testMode ? '?test=true' : '')).then(
          // Success
          function(response) {
            $rootScope.glassPane--;
          },
          function(response) {
            $rootScope.glassPane--;
            $rootScope.handleHttpError(response.data, response.status, response.headers,
              response.config);
          });
      }

      // Compute Workflow
      $scope.computeWorkflow = function() {
        $rootScope.glassPane++;
        $http({
          url : root_workflow + 'project/id/' + $scope.focusProject.id + '/compute',
          dataType : 'json',
          method : 'POST',
          headers : {
            'Content-Type' : 'application/json'
          }
        }).success(
          function(data) {
            $rootScope.glassPane--;
          },
          function(response) {
            $rootScope.glassPane--;
            $rootScope.handleHttpError(response.data, response.status, response.headers,
              response.config);
          })
      };

      // Start editing cycle
      // @Path("/project/id/{id:[0-9][0-9]*}/release/startEditing")
      $scope.startEditingCycle = function() {
        $rootScope.glassPane++;
        $http({
          url : root_mapping + 'project/id/' + $scope.focusProject.id + '/release/startEditing',
          dataType : 'json',
          method : 'POST',
          headers : {
            'Content-Type' : 'application/json'
          }
        }).then(
          function(data) {
            $scope.focusProject.editingCycleBeginDate = new Date();
            localStorageService.add('focusProject', $scope.focusProject);
            $rootScope.glassPane--;
          },
          function(response) {
            $rootScope.glassPane--;
            $rootScope.handleHttpError(response.data, response.status, response.headers,
              response.config);
          });
      }

    } ]);
