using Microsoft.EntityFrameworkCore;
using HomeFoodDelivery.Api.Data;

var builder = WebApplication.CreateBuilder(args);

// 1. Configure the Database Connection
builder.Services.AddDbContext<DataContext>(options =>
    options.UseSqlite(builder.Configuration.GetConnectionString("DefaultConnection")));

// 2. Add core API services
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

// 3. Enable the Swagger UI for all environments
// 3. Enable the Swagger UI for all environments
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI(); // <-- Removed the custom routing options
}

app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();

app.Run();