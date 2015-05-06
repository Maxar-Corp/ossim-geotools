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

//$inject("http://localhost/jasmine-test-injector/js/testiife.js", function() {
$inject("http://localhost:8080/tilestore/assets/app/admin/appAdmin.js", function() {

    //describe("Test $injector into appAdmin's IIFE", function() {
    //
    //    //it("variable 'name' inside loaded IIFE should be IIFE!", function() {
    //    //    expect(name).toBe("IIFE!");
    //    //});
    //    //
    //    //it("call function add(2,2) should return 4", function() {
    //    //    var result = add(2,2);
    //    //    expect(result).toBe(4);
    //    //});
    //    //
    //    //it("window.IIFE.sum should be 2", function() {
    //    //    expect(window.IIFE.sum).toBe(2);
    //    //});
    //
    //    //it("should return 'Hello from appAdmin!'", function(){
    //    //    expect(helloAdmin.response()).toEqual("Hello from appAdmin!") ;
    //    //});
    //
    //});

    describe("appAdmin tests", function(){

        //beforeEach(function() {
        //    //spyOn(jQuery,"ajax");
        //    //var objLayer = {
        //    //    minLevel: chance.natural({min: 0, max: 22}),
        //    //    maxLevel: chance.natural({min: 0, max: 22}),
        //    //    name: chance.word({syllables: 3}),
        //    //    epsgCode: "EPSG:3857"
        //    //}
        //});

        //it("Test if the create layer button was clicked" , function(){
        //    $('#submitCreateLayer').trigger('click');
        //    spyOn(window, 'ajaxCreateLayer')
        //    expect(window.ajaxCreateLayer).toHaveBeenCalled();
        //    //runs(function() {
        //    //    expect(ajaxCreateLayer).toHaveBeenCalled();
        //    //});
        //
        //});

        //it("calls the click() function", function() {
        //    //var cc = new CustomClass();
        //    spyOn($.fn, "click");
        //    $( '#submitCreateLayer' ).click();
        //    expect(cc.clickFunction()).toHaveBeenCalled();
        //});
        //it("should make a real AJAX request", function () {
        //
        //    var objLayer = {
        //        minLevel: chance.natural({min: 0, max: 22}),
        //        maxLevel: chance.natural({min: 0, max: 22}),
        //        name: chance.word({syllables: 3}),
        //        epsgCode: "EPSG:3857"
        //    }
        //
        //
        //    var callback = jasmine.createSpy();
        //    makeAjaxCall(callback);
        //    waitsFor(function() {
        //        return callback.callCount > 0;
        //    }, "The Ajax call timed out.", 5000);
        //
        //    runs(function() {
        //        expect(callback).toHaveBeenCalled();
        //    });
        //});

        //function makeAjaxCall(callback) {
        //    $.ajax({
        //        type: "GET",
        //        url: "data.json",
        //        contentType: "application/json; charset=utf-8"
        //        dataType: "json",
        //        success: callback
        //    });
        //}

        it("A successful ajax request to the 'createLayer' controller", function(){

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
                url: "/tilestore/layerManager/createLayer",
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

        it("A successful ajax request to the 'renameLayer' controller", function(){

            spyOn(jQuery,"ajax");

            var oldLayerNameTest = chance.word({syllables: 3});
            var newLayerNameTest = chance.word({syllables: 3});
            console.log(oldLayerNameTest + ' ' + newLayerNameTest);

            ajaxRenameLayer(oldLayerNameTest, newLayerNameTest);

            expect(jQuery.ajax).toHaveBeenCalledWith({
                url: "/tilestore/layerManager/renameLayer",
                type: 'POST',
                dataType: 'json',
                data: {'oldName': oldLayerNameTest, 'newName': newLayerNameTest}
            });
        });

        it("A successful ajax request to the 'deleteLayer' controller", function(){

            spyOn(jQuery,"ajax");

            var deleteLayerNameTest = chance.word({syllables: 3});
            console.log(deleteLayerNameTest);

            ajaxDeleteLayer(deleteLayerNameTest);

            expect(jQuery.ajax).toHaveBeenCalledWith({
                url: "/tilestore/layerManager/deleteLayer?",
                type: 'POST',
                dataType: 'json',
                data: {'name': deleteLayerNameTest}
            });
        });
    });
    //# sourceURL=appAdmin.js;
}); // end $inject

