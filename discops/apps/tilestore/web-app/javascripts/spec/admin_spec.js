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

    describe("Test $injector into appAdmin's IIFE", function() {

        //it("variable 'name' inside loaded IIFE should be IIFE!", function() {
        //    expect(name).toBe("IIFE!");
        //});
        //
        //it("call function add(2,2) should return 4", function() {
        //    var result = add(2,2);
        //    expect(result).toBe(4);
        //});
        //
        //it("window.IIFE.sum should be 2", function() {
        //    expect(window.IIFE.sum).toBe(2);
        //});

        it("should return 'Hello from appAdmin!'", function(){
            expect(helloAdmin.response()).toEqual("Hello from appAdmin!") ;
        });

    });

    describe("appAdmin tests", function(){

        it("should return switchUp fired", function(){
            expect(switchUp()).toEqual("switchUp fired!") ;
        });

        it("should return an $ajax result for creating a tile layer", function(){
            var d = $.Deferred();
            d.resolve([]);
            spyOn($, 'ajaxCreateLayer').and.returnValue(d.promise());


        })

    });
    //# sourceURL=appAdmin.js;
});










    //describe("appAdmin", function(){
    //    describe("when we are loading layers", function(){
    //        it("offers three crucial functions", function(){
    //            expect(readFixtures).toBeDefined();
    //        });
    //
    //        //it("can load fixtures from a file", function(){
    //        //   loadFixtures('')
    //        //});
    //
    //        it("should return hello from appAdmin", function(){
    //           expect(AppAdmin.hello.response()).toEqual("Hello from appAdmin!") ;
    //        });
    //
    //        it("should return switchUp fired", function(){
    //            expect(AppAdmin.switchUp()).toEqual("switchUp fired!") ;
    //        });
    //    });
    //});
