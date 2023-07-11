using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using TestDemonstrator.TicketRepository.Models;

namespace TestDemonstrator.TicketRepository.Data
{
    /// <summary>
    /// The following interface defines the characteristics of the
    /// database oriented ticket repository. The database is defined
    /// as a set of test ticket entities. The mapping between 
    /// <see cref="TestTicketEntity"/> and <see cref="ITestTicket"/>
    /// is defined from the <see cref="TicketMappingProfile"/>
    /// </summary>
    public interface ITestTicketContext
    {
        public DbSet<TestTicketEntity> Tickets { get; set; }
    }
}
