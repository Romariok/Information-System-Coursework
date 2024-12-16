interface PaginationProps {
  currentPage: number;
  hasMore: boolean;
  onPageChange: (page: number) => void;
}

export default function Pagination({
  currentPage,
  hasMore,
  onPageChange,
}: PaginationProps) {
  return (
    <div className="flex justify-center items-center space-x-4 mt-4">
      <button
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 1}
        className="px-3 py-1 rounded-md bg-gray-100 text-gray-600 disabled:opacity-50"
      >
        Previous
      </button>

      <span className="px-3 py-1">
        Page {currentPage}
      </span>

      <button
        onClick={() => onPageChange(currentPage + 1)}
        disabled={!hasMore}
        className="px-3 py-1 rounded-md bg-gray-100 text-gray-600 disabled:opacity-50"
      >
        Next
      </button>
    </div>
  );
}
