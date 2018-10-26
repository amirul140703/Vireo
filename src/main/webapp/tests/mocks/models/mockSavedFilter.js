var mockSavedFilter1 = {
    'id': 1
};

var mockSavedFilter2 = {
    'id': 2
};

var mockSavedFilter3 = {
    'id': 3
};

angular.module('mock.savedFilter', []).service('SavedFilter', function($q) {
    var model = this;
    var defer;
    var payloadResponse = function (payload) {
        return defer.resolve({
            body: angular.toJson({
                meta: {
                    status: 'SUCCESS'
                },
                payload: payload
            })
        });
    };

    model.isDirty = false;

    model.mock = function(toMock) {
        model.id = toMock.id;
    };

    model.clearValidationResults = function () {
    };

    model.delete = function() {
        defer = $q.defer();
        payloadResponse();
        return defer.promise;
    };

    model.dirty = function(boolean) {
        model.isDirty = boolean;
    };

    model.reload = function() {
    };

    model.save = function() {
        defer = $q.defer();
        payloadResponse();
        return defer.promise;
    };

    return model;
});
