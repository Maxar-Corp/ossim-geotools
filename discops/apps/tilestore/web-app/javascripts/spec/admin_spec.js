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

        beforeEach(function() {
            //spyOn(jQuery,"ajax");
            //var objLayer = {
            //    minLevel: chance.natural({min: 0, max: 22}),
            //    maxLevel: chance.natural({min: 0, max: 22}),
            //    name: chance.word({syllables: 3}),
            //    epsgCode: "EPSG:3857"
            //}
        });

        it("A successful ajax request to the createLayer should be sent", function(){

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


    });
    //# sourceURL=appAdmin.js;
});

