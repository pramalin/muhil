package com.alai.muhil.domain

object pagination {
    final case class Pagination(limit: Int, offset: Int)

    object Pagination {
      val defaultPageSize = 20

      def apply(maybeLimit: Option[Int], maybeOffset: Option[Int]) =
        new Pagination(maybeLimit.getOrElse(defaultPageSize), offset = maybeOffset.getOrElse(0))

      def default =
        new Pagination(limit = defaultPageSize, offset = 0)
    }
}