$(function(){

    $('#btnSeating').on('click', createseating);

    $(function(){
        $('.seat').on('click',function(){
            var selectedSeats = $("#tickets").val()
            if (selectedSeats) {
                selectedSeats = selectedSeats + ","
            }
            $("#tickets").val(selectedSeats + $(this).attr("data-value"))
            if($(this).hasClass( "selected" )){
                $( this ).removeClass( "selected" );
            }else{
                $( this ).addClass( "selected" );
            }

        });

        $('.seat').mouseenter(function(){
            $( this ).addClass( "hovering" );

            $('.seat').mouseleave(function(){
                $( this ).removeClass( "hovering" );

            });
        });


    });

});
//Note:In js the outer loop runs first then the inner loop runs completely so it goes o.l. then i.l. i.l .i.l .i.l. i.l etc and repeat

function createseating(){

    var seatingValue = [];
    for ( var i = 0; i < 10; i++){

        for (var j=0; j<10; j++){
            var seatingStyle = "<div class='seat available'></div>";
            seatingValue.push(seatingStyle);

            if ( j === 9){
                console.log("hi");
                var seatingStyle = "<div class='clearfix'></div>";
                seatingValue.push(seatingStyle);



            }
        }
    }

    $('#messagePanel').html(seatingValue);


};