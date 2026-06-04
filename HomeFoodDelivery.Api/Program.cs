using Microsoft.EntityFrameworkCore;
using HomeFoodDelivery.Api.Data;
using HomeFoodDelivery.Api.Services;
using HomeFoodDelivery.Api.Hubs; // <-- Add this

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddDbContext<DataContext>(options =>
    options.UseSqlite(builder.Configuration.GetConnectionString("DefaultConnection")));

// CORS
builder.Services.AddCors(options =>
{
    // Important: SignalR requires AllowCredentials() if you are using specific origins, 
    // but for our local "AllowAll" test, we can use SetIsOriginAllowed.
    options.AddPolicy("AllowAll", policy =>
        policy.SetIsOriginAllowed(origin => true) // Allow any local file origin
              .AllowAnyMethod()
              .AllowAnyHeader()
              .AllowCredentials()); // <-- SignalR needs this
});

builder.Services.AddControllers();
builder.Services.AddScoped<IOrderService, OrderService>();
builder.Services.AddSignalR(); // <-- 1. TURN ON SIGNALR

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}


app.UseHttpsRedirection();

// 1. ADD THIS FIRST: Tells the server that "index.html" is the main homepage
app.UseDefaultFiles();

// 2. THEN THIS: Tells the server it is allowed to serve files from the wwwroot folder
app.UseStaticFiles();

app.UseCors("AllowAll");
app.UseAuthorization();
app.MapControllers();
app.MapHub<KitchenHub>("/kitchenHub");

app.Run();