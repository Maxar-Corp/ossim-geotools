/**
 * Created by adrake on 4/30/15.
 */
// start jasmine unit testing
describe("jasmine-test-injector", function() {
    "use strict";

    beforeEach(function() {
        spyOn(window, "$inject");
    });

    it("$inject should be a global function", function() {
        expect(typeof $inject).toBe("function");
    });

    it("$inject should call through", function() {
        $inject();
        expect($inject).toHaveBeenCalled();
    });

});

// setup $inject to use jasmine
$inject.use.jasmine();

$inject("http://localhost:8080/tilestore/assets/app/admin/omarWfs.js", function() {
    describe("appAdmin tests", function() {
        it("the foo function should return an id", function () {
            expect(foo()).toEqual(1);
        });
    });
});

//$inject("http://localhost/jasmine-test-injector/js/testiife.js", function() {
$inject("http://localhost:8080/tilestore/assets/app/admin/appAdmin.js", function() {

    describe("appAdmin tests", function(){

        it("A successful ajax request to the layerManager 'create' method", function(){

            spyOn(jQuery,"ajax");

            var objLayer = {
                minLevel: chance.natural({min: 0, max: 22}),
                maxLevel: chance.natural({min: 0, max: 22}),
                name: chance.word({syllables: 3}),
                epsgCode: "EPSG:3857"
            }

            console.log(objLayer);
            ajaxCreateLayer(objLayer);

            expect(jQuery.ajax).toHaveBeenCalledWith({
                url: "/tilestore/layerManager/create",
                type: 'POST',
                dataType: 'json',
                data: objLayer
            });

            //var callback = jasmine.createSpy();
            //waitsFor(function() {
            //    return callback.callCount > 0;
            //}, "The Ajax call timed out.", 5000);

            //runs(function() {
            //    expect(callback).toHaveBeenCalled();
            //});

        });

        it("A successful ajax request to the layerManager 'rename' method", function(){

            spyOn(jQuery,"ajax");

            var oldLayerNameTest = chance.word({syllables: 3});
            var newLayerNameTest = chance.word({syllables: 3});
            console.log(oldLayerNameTest + ' ' + newLayerNameTest);

            ajaxRenameLayer(oldLayerNameTest, newLayerNameTest);

            expect(jQuery.ajax).toHaveBeenCalledWith({
                url: "/tilestore/layerManager/rename",
                type: 'POST',
                dataType: 'json',
                data: {'oldName': oldLayerNameTest, 'newName': newLayerNameTest}
            });
        });

        it("A successful ajax request to the layerManager 'delete' method", function(){

            spyOn(jQuery,"ajax");

            var deleteLayerNameTest = chance.word({syllables: 3});
            console.log(deleteLayerNameTest);

            ajaxDeleteLayer(deleteLayerNameTest);

            expect(jQuery.ajax).toHaveBeenCalledWith({
                url: "/tilestore/layerManager/delete",
                type: 'POST',
                dataType: 'json',
                data: {'name': deleteLayerNameTest}
            });
        });

    });
    //# sourceURL=appAdmin.js;
}); // end $inject



