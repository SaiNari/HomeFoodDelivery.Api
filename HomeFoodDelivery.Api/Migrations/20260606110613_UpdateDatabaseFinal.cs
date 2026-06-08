using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace HomeFoodDelivery.Api.Migrations
{
    /// <inheritdoc />
    public partial class UpdateDatabaseFinal : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "CookFollowers",
                columns: table => new
                {
                    Id = table.Column<int>(type: "INTEGER", nullable: false)
                        .Annotation("Sqlite:Autoincrement", true),
                    CustomerId = table.Column<int>(type: "INTEGER", nullable: false),
                    CookId = table.Column<int>(type: "INTEGER", nullable: false),
                    CreatedAt = table.Column<DateTime>(type: "TEXT", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_CookFollowers", x => x.Id);
                    table.ForeignKey(
                        name: "FK_CookFollowers_Users_CookId",
                        column: x => x.CookId,
                        principalTable: "Users",
                        principalColumn: "UserId",
                        onDelete: ReferentialAction.Restrict);
                    table.ForeignKey(
                        name: "FK_CookFollowers_Users_CustomerId",
                        column: x => x.CustomerId,
                        principalTable: "Users",
                        principalColumn: "UserId",
                        onDelete: ReferentialAction.Restrict);
                });

            migrationBuilder.CreateIndex(
                name: "IX_CookFollowers_CookId",
                table: "CookFollowers",
                column: "CookId");

            migrationBuilder.CreateIndex(
                name: "IX_CookFollowers_CustomerId",
                table: "CookFollowers",
                column: "CustomerId");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "CookFollowers");
        }
    }
}
