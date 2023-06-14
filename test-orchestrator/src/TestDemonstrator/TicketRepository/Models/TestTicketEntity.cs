using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace TestDemonstrator.TicketRepository.Models
{
    public class TestTicketEntity
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public Guid TicketId { get; set; }

        [Required]
        public DateTime CreatedAt { get; set; }

        public DateTime? UpdatedAt { get; set; }

        [Required]
        public string Type { get; set; }

        public string? Uri { get; set; }

        //[Required]
        //public string IdShort { get; set; }

        [Column(TypeName = "xml")]
        public string? PassiveShell { get; set; }

        [Required]
        [Column(TypeName = "xml")]
        public string StructureDescriptor { get; set; }

        [Column(TypeName = "xml")]
        public string? StateMachineDescriptor { get; set; }

        [Column(TypeName = "xml")]
        public string? TestReport { get; set; }
    }
}
