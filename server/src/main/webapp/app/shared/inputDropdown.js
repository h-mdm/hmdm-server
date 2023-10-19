// This one is updated, bugs are fixed
// So we don't use the one from bower
angular.module('inputDropdown', []).directive('inputDropdown', [function() {
  var templateString =
  '<div class="input-dropdown">' +
    '<input type="text"' +
           'name="{{inputName}}"' +
           'placeholder="{{inputPlaceholder}}"' +
           'autocomplete="off"' +
           'ng-model="inputValue"' +
           'ng-required="inputRequired"' +
           'ng-change="inputChange()"' +
           'ng-focus="inputFocus()"' +
           'ng-blur="inputBlur($event)"' +
           'input-dropdown-validator>' +
     '<ul ng-show="dropdownVisible">' +
      '<li ng-repeat="item in dropdownItems"' +
          'ng-click="selectItem(item)"' +
          'ng-mouseenter="setActive($index)"' +
          'ng-mousedown="dropdownPressed()"' +
          'ng-class="{\'active\': activeItemIndex === $index}"' +
          '>' +
        '<span ng-if="item.readableName">{{item.readableName}}</span>' +
        '<span ng-if="!item.readableName">{{item}}</span>' +
      '</li>' +
    '</ul>' +
  '</div>';

  return {
    restrict: 'E',
    scope: {
      defaultDropdownItems: '=',
      selectedItem: '=',
      allowCustomInput: '=',
      inputRequired: '=',
      inputName: '@',
      inputPlaceholder: '@',
      filterListMethod: '&',
      itemSelectedMethod: '&'
    },
    template: templateString,
    controller: function($scope) {
      this.getSelectedItem = function() {
        return $scope.selectedItem;
      };
      this.isRequired = function() {
        return $scope.inputRequired;
      };
      this.customInputAllowed = function() {
        return $scope.allowCustomInput;
      };
      this.getInput = function() {
        return $scope.inputValue;
      };
    },
    link: function(scope, element) {
      var pressedDropdown = false;
      var inputScope = element.find('input').isolateScope();

      scope.activeItemIndex = 0;
      scope.inputValue = '';
      scope.dropdownVisible = false;
      scope.dropdownItems = scope.defaultDropdownItems || [];

      scope.$watch('dropdownItems', function(newValue, oldValue) {
        if (!angular.equals(newValue, oldValue)) {
          // If new dropdownItems were retrieved, reset active item
          if (scope.allowCustomInput) {
            scope.setInputActive();
          }
          else {
            scope.setActive(0);
          }
        }
      });

      scope.$watch('selectedItem', function(newValue, oldValue) {
        inputScope.updateInputValidity();

        // Fixed by seva 17.10.2023 to initialize it with a non-empty value
        if (/*!angular.equals(newValue, oldValue)*/true) {
          if (newValue) {
            // Update value in input field to match readableName of selected item
            if (typeof newValue === 'string') {
              scope.inputValue = newValue;
            }
            else {
              scope.inputValue = newValue.readableName;
            }
          }
          else {
            // Uncomment to clear input field when editing it after making a selection
            // scope.inputValue = '';
          }
        }
      });

      scope.setInputActive = function() {
        scope.setActive(-1);

        //TODO: Add active/selected class to input field for styling
      };

      scope.setActive = function(itemIndex) {
        scope.activeItemIndex = itemIndex;
      };

      scope.inputChange = function() {
        scope.selectedItem = null;
        showDropdown();

        if (!scope.inputValue) {
          scope.dropdownItems = scope.defaultDropdownItems || [];
          return;
        }
        else if (scope.allowCustomInput) {
          inputScope.updateInputValidity();
            // Fixed by seva 17.10.2023 to allow custom input
            scope.selectedItem = scope.inputValue;
        }

        if (scope.filterListMethod) {
          var promise = scope.filterListMethod({userInput: scope.inputValue});
          if (promise) {
            promise.then(function(dropdownItems) {
              scope.dropdownItems = dropdownItems;
            });
          }
        }
      };

      scope.inputFocus = function() {
        if (scope.allowCustomInput) {
          scope.setInputActive();
        }
        else {
          scope.setActive(0);
        }
        showDropdown();
      };

      scope.inputBlur = function(event) {
        if (pressedDropdown) {
          // Blur event is triggered before click event, which means a click on a dropdown item wont be triggered if we hide the dropdown list here.
          pressedDropdown = false;
          return;
        }
        hideDropdown();
      };

      scope.dropdownPressed = function() {
        pressedDropdown = true;
      };

      scope.selectItem = function(item) {
        scope.selectedItem = item;
        hideDropdown();
        scope.dropdownItems = [item];

        if (scope.itemSelectedMethod) {
          scope.itemSelectedMethod({item: item});
        }
      };

      var showDropdown = function () {
        scope.dropdownVisible = true;
      };
      var hideDropdown = function() {
        scope.dropdownVisible = false;
      };

      var selectPreviousItem = function() {
        var prevIndex = scope.activeItemIndex - 1;
        if (prevIndex >= 0) {
          scope.setActive(prevIndex);
        }
        else if (scope.allowCustomInput) {
          scope.setInputActive();
        }
      };

      var selectNextItem = function() {
        var nextIndex = scope.activeItemIndex + 1;
        if (nextIndex < scope.dropdownItems.length) {
          scope.setActive(nextIndex);
        }
      };

      var selectActiveItem = function()  {
        if (scope.activeItemIndex >= 0 && scope.activeItemIndex < scope.dropdownItems.length) {
          scope.selectItem(scope.dropdownItems[scope.activeItemIndex]);
        }
        else if (scope.allowCustomInput && scope.activeItemIndex === -1) {
          //TODO: Select user input. Do we need to call the controller here (ie scope.itemSelectedMethod()) or is it enough to just leave the input value in the field?
        }
      };

      element.bind("keydown keypress", function (event) {
        switch (event.which) {
          case 38: //up
            scope.$apply(selectPreviousItem);
            break;
          case 40: //down
            scope.$apply(selectNextItem);
            break;
          case 13: // return
            if (scope.dropdownVisible && scope.dropdownItems && scope.dropdownItems.length > 0 && scope.activeItemIndex !== -1) {
              // only preventDefault when there is a list so that we can submit form with return key after a selection is made
              event.preventDefault();
              scope.$apply(selectActiveItem);
            }
            break;
        }
      });
    }
  }
}]);

angular.module('inputDropdown').directive('inputDropdownValidator', function() {
  return {
    require: ['^inputDropdown', 'ngModel'],
    restrict: 'A',
    scope: {},
    link: function(scope, element, attrs, ctrls) {
      var inputDropdownCtrl = ctrls[0];
      var ngModelCtrl = ctrls[1];
      var validatorName = 'itemSelectedValid';

      scope.updateInputValidity = function() {
        var selection = inputDropdownCtrl.getSelectedItem();
        var isValid = false;

        if (!inputDropdownCtrl.isRequired()) {
          // Input isn't required, so it's always valid
          isValid = true;
        }
        else if (inputDropdownCtrl.customInputAllowed() && inputDropdownCtrl.getInput()) {
          // Custom input is allowed so we just need to make sure the input field isn't empty
          isValid = true;
        }
        else if (selection) {
          // Input is required and custom input is not allowed, so only validate if an item is selected
          isValid = true;
        }

        ngModelCtrl.$setValidity(validatorName, isValid);
      };
    }
  };
});
