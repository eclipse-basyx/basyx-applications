using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TestDemonstrator.TicketRepository.Models;

namespace TestDemonstrator.TicketRepository.Data
{
    /// <summary>
    /// The following class is used for the dynamic creation of
    /// the database. For more information read the EntityFrameworkCore documentation.
    /// </summary>
    public class TestTicketContext : DbContext, ITestTicketContext
    {
        public TestTicketContext() { }

        public TestTicketContext(DbContextOptions options)
            : base(options) { }

        public DbSet<TestTicketEntity> Tickets { get; set; }

        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        {
            base.OnConfiguring(optionsBuilder);
        }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);
        }
    }
}
