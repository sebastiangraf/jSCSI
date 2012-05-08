<!--

    Copyright (c) 2012, University of Konstanz, Distributed Systems Group
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:
        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of the University of Konstanz nor the
          names of its contributors may be used to endorse or promote products
          derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
    DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

-->
<xsl:stylesheet version = '1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:msxsl="urn:schemas-microsoft-com:xslt">
<xsl:output method="html" encoding="UTF-8"/>
<xsl:key name="cat" match="category" use="@name"/>
<xsl:template match="/">
<xsl:for-each select="site">
	<html>
	<head>
	<title>WhiskasUpdateSite</title>
	<style>@import url("web/site.css");</style>
	</head>
	<body>
	<h1 class="title">WhiskasUpdateSite</h1>
	<p class="bodyText"><xsl:value-of select="description"/></p>
	<table width="100%" border="0" cellspacing="1" cellpadding="2">
	<xsl:for-each select="category-def">
		<xsl:sort select="@label" order="ascending" case-order="upper-first"/>
		<xsl:sort select="@name" order="ascending" case-order="upper-first"/>
	<xsl:if test="count(key('cat',@name)) != 0">
			<tr class="header">
				<td class="sub-header" width="30%">
					<xsl:value-of select="@name"/>
				</td>
				<td class="sub-header" width="70%">
					<xsl:value-of select="@label"/>
				</td>
			</tr>
			<xsl:for-each select="key('cat',@name)">
			<xsl:sort select="ancestor::feature//@version" order="ascending"/>
			<xsl:sort select="ancestor::feature//@id" order="ascending" case-order="upper-first"/>
			<tr>
				<xsl:choose>
				<xsl:when test="(position() mod 2 = 1)">
					<xsl:attribute name="class">dark-row</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="class">light-row</xsl:attribute>
				</xsl:otherwise>
				</xsl:choose>
				<td class="log-text" id="indent">
						<xsl:choose>
						<xsl:when test="ancestor::feature//@label">
							<a href="{ancestor::feature//@url}"><xsl:value-of select="ancestor::feature//@label"/></a>
							<br/>
							<div id="indent">
							(<xsl:value-of select="ancestor::feature//@id"/> - <xsl:value-of select="ancestor::feature//@version"/>)
							</div>
						</xsl:when>
						<xsl:otherwise>
						<a href="{ancestor::feature//@url}"><xsl:value-of select="ancestor::feature//@id"/> - <xsl:value-of select="ancestor::feature//@version"/></a>
						</xsl:otherwise>
						</xsl:choose>
						<br />
				</td>
				<td>
					<table>
						<xsl:if test="ancestor::feature//@os">
							<tr><td class="log-text" id="indent">Operating Systems:</td>
							<td class="log-text" id="indent"><xsl:value-of select="ancestor::feature//@os"/></td>
							</tr>
						</xsl:if>
						<xsl:if test="ancestor::feature//@ws">
							<tr><td class="log-text" id="indent">Windows Systems:</td>
							<td class="log-text" id="indent"><xsl:value-of select="ancestor::feature//@ws"/></td>
							</tr>
						</xsl:if>
						<xsl:if test="ancestor::feature//@nl">
							<tr><td class="log-text" id="indent">Languages:</td>
							<td class="log-text" id="indent"><xsl:value-of select="ancestor::feature//@nl"/></td>
							</tr>
						</xsl:if>
						<xsl:if test="ancestor::feature//@arch">
							<tr><td class="log-text" id="indent">Architecture:</td>
							<td class="log-text" id="indent"><xsl:value-of select="ancestor::feature//@arch"/></td>
							</tr>
						</xsl:if>
					</table>
				</td>
			</tr>
			</xsl:for-each>
			<tr><td class="spacer"><br/></td><td class="spacer"><br/></td></tr>
		</xsl:if>
	</xsl:for-each>
	<xsl:if test="count(feature)  &gt; count(feature/category)">
	<tr class="header">
		<td class="sub-header" colspan="2">
		Uncategorized
		</td>
	</tr>
	</xsl:if>
	<xsl:choose>
	<xsl:when test="function-available('msxsl:node-set')">
	   <xsl:variable name="rtf-nodes">
		<xsl:for-each select="feature[not(category)]">
			<xsl:sort select="@id" order="ascending" case-order="upper-first"/>
			<xsl:sort select="@version" order="ascending" />
			<xsl:value-of select="."/>
			<xsl:copy-of select="." />
		</xsl:for-each>
	   </xsl:variable>
	   <xsl:variable name="myNodeSet" select="msxsl:node-set($rtf-nodes)/*"/>
	<xsl:for-each select="$myNodeSet">
	<tr>
		<xsl:choose>
		<xsl:when test="position() mod 2 = 1">
		<xsl:attribute name="class">dark-row</xsl:attribute>
		</xsl:when>
		<xsl:otherwise>
		<xsl:attribute name="class">light-row</xsl:attribute>
		</xsl:otherwise>
		</xsl:choose>
		<td class="log-text" id="indent">
			<xsl:choose>
			<xsl:when test="@label">
				<a href="{@url}"><xsl:value-of select="@label"/></a>
				<br />
				<div id="indent">
				(<xsl:value-of select="@id"/> - <xsl:value-of select="@version"/>)
				</div>
			</xsl:when>
			<xsl:otherwise>
				<a href="{@url}"><xsl:value-of select="@id"/> - <xsl:value-of select="@version"/></a>
			</xsl:otherwise>
			</xsl:choose>
			<br /><br />
		</td>
		<td>
			<table>
				<xsl:if test="@os">
					<tr><td class="log-text" id="indent">Operating Systems:</td>
					<td class="log-text" id="indent"><xsl:value-of select="@os"/></td>
					</tr>
				</xsl:if>
				<xsl:if test="@ws">
					<tr><td class="log-text" id="indent">Windows Systems:</td>
					<td class="log-text" id="indent"><xsl:value-of select="@ws"/></td>
					</tr>
				</xsl:if>
				<xsl:if test="@nl">
					<tr><td class="log-text" id="indent">Languages:</td>
					<td class="log-text" id="indent"><xsl:value-of select="@nl"/></td>
					</tr>
				</xsl:if>
				<xsl:if test="@arch">
					<tr><td class="log-text" id="indent">Architecture:</td>
					<td class="log-text" id="indent"><xsl:value-of select="@arch"/></td>
					</tr>
				</xsl:if>
			</table>
		</td>
	</tr>
	</xsl:for-each>
	</xsl:when>
	<xsl:otherwise>
	<xsl:for-each select="feature[not(category)]">
	<xsl:sort select="@id" order="ascending" case-order="upper-first"/>
	<xsl:sort select="@version" order="ascending" />
	<tr>
		<xsl:choose>
		<xsl:when test="count(preceding-sibling::feature[not(category)]) mod 2 = 1">
		<xsl:attribute name="class">dark-row</xsl:attribute>
		</xsl:when>
		<xsl:otherwise>
		<xsl:attribute name="class">light-row</xsl:attribute>
		</xsl:otherwise>
		</xsl:choose>
		<td class="log-text" id="indent">
			<xsl:choose>
			<xsl:when test="@label">
				<a href="{@url}"><xsl:value-of select="@label"/></a>
				<br />
				<div id="indent">
				(<xsl:value-of select="@id"/> - <xsl:value-of select="@version"/>)
				</div>
			</xsl:when>
			<xsl:otherwise>
				<a href="{@url}"><xsl:value-of select="@id"/> - <xsl:value-of select="@version"/></a>
			</xsl:otherwise>
			</xsl:choose>
			<br /><br />
		</td>
		<td>
			<table>
				<xsl:if test="@os">
					<tr><td class="log-text" id="indent">Operating Systems:</td>
					<td class="log-text" id="indent"><xsl:value-of select="@os"/></td>
					</tr>
				</xsl:if>
				<xsl:if test="@ws">
					<tr><td class="log-text" id="indent">Windows Systems:</td>
					<td class="log-text" id="indent"><xsl:value-of select="@ws"/></td>
					</tr>
				</xsl:if>
				<xsl:if test="@nl">
					<tr><td class="log-text" id="indent">Languages:</td>
					<td class="log-text" id="indent"><xsl:value-of select="@nl"/></td>
					</tr>
				</xsl:if>
				<xsl:if test="@arch">
					<tr><td class="log-text" id="indent">Architecture:</td>
					<td class="log-text" id="indent"><xsl:value-of select="@arch"/></td>
					</tr>
				</xsl:if>
			</table>
		</td>
	</tr>
	</xsl:for-each>
	</xsl:otherwise>
	</xsl:choose>
	</table>
	</body>
	</html>
</xsl:for-each>
</xsl:template>
</xsl:stylesheet>
