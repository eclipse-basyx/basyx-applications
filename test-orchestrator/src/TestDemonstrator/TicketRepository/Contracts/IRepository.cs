namespace TestDemonstrator.TicketRepository.Contracts
{
    /// <summary>
    /// The following interface represents a generic abstraction for the <see cref="ITicketRepository"/> interface
    /// </summary>
    /// <typeparam name="T">The type of elements the repository should hold</typeparam>
    public interface IRepository<T>
    {
        /// <summary>
        /// Uploads an element to the repository
        /// </summary>
        /// <param name="ticket">The element to upload</param>
        void Upload(T ticket);

        /// <summary>
        /// Updates an element of the repository
        /// </summary>
        /// <param name="ticket">The element to update</param>
        void Update(T ticket);

        /// <summary>
        /// Deletes an element of the repository
        /// </summary>
        /// <param name="ticket">The element to delete</param>
        void Delete(T ticket);
    }
}
