using Microsoft.EntityFrameworkCore;
using HomeFoodDelivery.Api.Data;
using HomeFoodDelivery.Api.Services;
using HomeFoodDelivery.Api.Hubs; 

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddDbContext<DataContext>(options =>
    options.UseSqlite("Data Source=HomeFoodDelivery.db"));

builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", policy =>
        policy.SetIsOriginAllowed(origin => true) 
              .AllowAnyMethod()
              .AllowAnyHeader()
              .AllowCredentials()); 
});

builder.Services.AddControllers()
    .AddJsonOptions(options =>
    {
        // This line tells the API to stop looping when it sees the same object twice
        options.JsonSerializerOptions.ReferenceHandler = System.Text.Json.Serialization.ReferenceHandler.IgnoreCycles;
        options.JsonSerializerOptions.DefaultIgnoreCondition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull;
    });
builder.Services.AddScoped<IOrderService, OrderService>();
builder.Services.AddScoped<IWalletService, WalletService>();
builder.Services.AddSignalR(); 

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}


app.UseHttpsRedirection();

app.UseDefaultFiles();

app.UseStaticFiles();

app.UseCors("AllowAll");
app.UseAuthorization();
app.MapControllers();
app.MapHub<KitchenHub>("/kitchenHub");

app.Run();