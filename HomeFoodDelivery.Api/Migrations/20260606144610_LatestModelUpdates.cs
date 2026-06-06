using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace HomeFoodDelivery.Api.Migrations
{
    /// <inheritdoc />
    public partial class LatestModelUpdates : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "CookFavorites",
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
                    table.PrimaryKey("PK_CookFavorites", x => x.Id);
                    table.ForeignKey(
                        name: "FK_CookFavorites_Users_CookId",
                        column: x => x.CookId,
                        principalTable: "Users",
                        principalColumn: "UserId",
                        onDelete: ReferentialAction.Restrict);
                    table.ForeignKey(
                        name: "FK_CookFavorites_Users_CustomerId",
                        column: x => x.CustomerId,
                        principalTable: "Users",
                        principalColumn: "UserId",
                        onDelete: ReferentialAction.Restrict);
                });

            migrationBuilder.CreateIndex(
                name: "IX_CookFavorites_CookId",
                table: "CookFavorites",
                column: "CookId");

            migrationBuilder.CreateIndex(
                name: "IX_CookFavorites_CustomerId",
                table: "CookFavorites",
                column: "CustomerId");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "CookFavorites");
        }
    }
}
