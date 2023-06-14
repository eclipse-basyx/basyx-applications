using TestDemonstrator.Ticket;
using TestDemonstrator.Ticket.Contracts;

namespace TestDemonstrator.Tests.TicketTests
{
    public class TicketTests
    {
        private ITestTicket? ticket = null;

        [SetUp]
        public void Setup()
        {
            ticket = new TestTicket();
        }

        [Test]
        public void InstantiateTestTicket_ReturnsTicketWithGuidAndCreationTimestamp()
        {
            Assert.IsNotNull(ticket!.Id);
            Assert.IsNotNull(ticket!.CreatedAt);
        }
    }
}
